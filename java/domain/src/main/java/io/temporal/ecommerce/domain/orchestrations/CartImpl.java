package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.activity.ActivityOptions;
import io.temporal.ecommerce.messages.commands.SetCartItemsRequest;
import io.temporal.ecommerce.messages.commands.WriteDenormalizedCartItemRequest;
import io.temporal.ecommerce.messages.queries.CartResponse;
import io.temporal.ecommerce.messages.queries.ProductInventoryStatusRequest;
import io.temporal.ecommerce.messages.queries.ProductShipabilityRequest;
import io.temporal.ecommerce.messages.values.CartItem;
import io.temporal.ecommerce.messages.values.ProductQuantity;
import io.temporal.ecommerce.messages.workflows.InitializeCartRequest;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.*;
import org.slf4j.Logger;

public class CartImpl implements Cart {
  private final ViewActivities viewHandlers;
  private CartResponse state;
  private final List<SetCartItemsRequest> setItemsRequests = new ArrayList<>();
  private final InventoryActivities inventoryHandlers;
  private final ShippingActivities shippingHandlers;
  private Logger logger = Workflow.getLogger(CartImpl.class);

  public CartImpl() {
    this.inventoryHandlers =
        Workflow.newActivityStub(
            InventoryActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());
    this.shippingHandlers =
        Workflow.newActivityStub(
            ShippingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());
    this.viewHandlers =
        Workflow.newActivityStub(
            ViewActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());
  }

  @Override
  public void execute(InitializeCartRequest params) {
    this.state = new CartResponse(params.id(), params.userId(), false, new LinkedHashMap<>());

    while (!this.state.isSealed()) {
      Workflow.await(() -> !this.setItemsRequests.isEmpty());
      var items = this.processItemChanges(state, this.setItemsRequests);
      // note we are not trying to cover the "removed from cart" path where inventory or shipping
      // status changed
      var valid = validateCartItems(items);
      this.state =
          new CartResponse(this.state.id(), this.state.userId(), this.state.isSealed(), valid);
      // source of truth is our Workflow state (not the view models)
      syncCartView(this.state);
    }
  }

  private void syncCartView(CartResponse state) {
    var ps = new ArrayList<Promise<Void>>();
    var errors = new ArrayList<RuntimeException>();
    for (Map.Entry<String, CartItem> entry : state.items().entrySet()) {
      var req = new WriteDenormalizedCartItemRequest(state.userId(), entry.getValue());
      ps.add(
          Async.procedure(viewHandlers::writeDenormalizedCartItem, req)
              .handle(
                  (v, e) -> {
                    if (e != null) {
                      errors.add(e);
                    }
                    return null;
                  }));
    }
    // wait for all the views to be written before returning
    Promise.allOf(ps).get();
    if (!errors.isEmpty()) {
      logger.error("Error occurred while syncing cart view: {}", errors);
      // what should we do when we cant sync our view state from domain??
    }
  }

  // setItems allows us to add, remove or clear a product from our collection
  // quantity of 0 means to clear the item
  // quantity of < 0 means to reduce the current quantity by Q
  // quantity of > 0 means to append the item with quantity of Q
  @Override
  public void setItems(SetCartItemsRequest params) {
    this.setItemsRequests.add(params);
  }

  @Override
  public CartResponse getState() {
    return this.state;
  }
  
  private Map<String, CartItem> processItemChanges(
      CartResponse state, List<SetCartItemsRequest> requests) {
    var result = new LinkedHashMap<String, CartItem>();
    Iterator<SetCartItemsRequest> it = requests.iterator();
    while (it.hasNext()) {
      SetCartItemsRequest request = it.next();
      // just exclude zero quantity delta requests
      for (ProductQuantity q : request.productQuantities().stream().filter(q -> q.quantity() != 0).toList()) {
        var item = state.items().get(q.productId());
        var qtyDelta = q.quantity();
        if (item != null) {
          qtyDelta = item.quantity() + q.quantity();
        }
        result.put(q.productId(), new CartItem(state.id(), q.productId(), qtyDelta));
      }
      it.remove();
    }
    return result;
  }

  private Map<String, CartItem> validateCartItems(Map<String, CartItem> items) {
    var valid = new LinkedHashMap<String, CartItem>();
    for (Map.Entry<String, CartItem> e : items.entrySet()) {
      var check =
          validateProduct(new ProductQuantity(e.getValue().productId(), e.getValue().quantity()));
      if (check != null) {
        valid.put(
            e.getKey(),
            new CartItem(e.getValue().cartId(), e.getValue().productId(), check.quantity()));
      }
    }
    return valid;
  }


  // validateProductQuantity
  // Not sure what the rules are so these are the ones I made up:
  // 1. if the quantity available is less than requested, allow up to that maximum available amount
  // 2. if the item is not shippable, return `null` and do not accept it as an cart item. UI should
  // show this as unavailable elsewhere
  private ProductQuantity validateProduct(ProductQuantity productQuantity) {
    // we are validating here _in the workflow_ but a debate should be had on
    // whether the inventory subdomain should do an assertion instead in the handler.
    var inv =
        inventoryHandlers.getInventoryStatus(
            new ProductInventoryStatusRequest(productQuantity.productId()));
    if (inv.quantityOrderable() < productQuantity.quantity()) {
      // what shall we do with an invalid/out-of-stock item?
      // 1. enqueue on a "outOfStockItems" list and update state?
      // 2. error the entire transaction?
      // 3. adjust the append request to suit available quantity? We will do that here, but customer
      // should know
      return new ProductQuantity(productQuantity.productId(), inv.quantityOrderable());
    }
    var ship =
        shippingHandlers.getProductShipability(
            new ProductShipabilityRequest(productQuantity.productId()));
    if (!ship.isShippable()) {
      return null;
    }
    return productQuantity;
  }
}
