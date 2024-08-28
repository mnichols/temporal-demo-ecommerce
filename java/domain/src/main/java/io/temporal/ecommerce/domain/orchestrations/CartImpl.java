package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.activity.ActivityOptions;
import io.temporal.ecommerce.messages.api.InitializeCartRequest;
import io.temporal.ecommerce.messages.commands.AppendCartItemsRequest;
import io.temporal.ecommerce.messages.queries.CartResponse;
import io.temporal.ecommerce.messages.queries.ProductInventoryStatusRequest;
import io.temporal.ecommerce.messages.queries.ProductShipabilityRequest;
import io.temporal.ecommerce.messages.values.CartItem;
import io.temporal.ecommerce.messages.values.ProductQuantity;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.*;

public class CartImpl implements Cart{
    private final ViewActivities viewHandlers;
    private CartResponse state;
    private final List<AppendCartItemsRequest> appendItemRequests = new ArrayList<>();
    private final InventoryActivities inventoryHandlers;
    private final ShippingActivities shippingHandlers;
    private Logger logger = Workflow.getLogger(CartImpl.class);
    public CartImpl() {
        this.inventoryHandlers = Workflow.newActivityStub(InventoryActivities.class, ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(10)).build());
        this.shippingHandlers = Workflow.newActivityStub(ShippingActivities.class, ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(10)).build());
        this.viewHandlers = Workflow.newActivityStub(ViewActivities.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());
    }

    @Override
    public void execute(InitializeCartRequest params) {
        this.state = new CartResponse(
                params.id(),
                params.userId(),
                false,
                new LinkedHashMap<>()
        );

        while(!this.state.isSealed()){
            Workflow.await(()-> !this.appendItemRequests.isEmpty());
            // process enqueued messages
            var items = merge(this.state.items(), processAppends(this.appendItemRequests));
            // source of truth is our Workflow state (not the view models)
            this.state = new CartResponse(Workflow.getInfo().getWorkflowId(), this.state.userId(), this.state.isSealed(), items);
            syncCartView();
        }
    }

    private void syncCartView() {
        var ps = new ArrayList<Promise<Void>>();
        var errors = new ArrayList<RuntimeException>();
        for (Map.Entry<String, CartItem> entry : this.state.items().entrySet()) {
            ps.add(Async.procedure(viewHandlers::writeDenormalizedCartItem, entry.getValue()).handle((v,e) -> {
                if(e != null) { errors.add(e);}
                return null;
            }));
        }
        // wait for all the views to be written before returning
        Promise.allOf(ps).get();
        if(!errors.isEmpty()) {
            logger.error("Error occurred while syncing cart view: {}", errors);
            // what should we do when we cant sync our view state from domain??
        }
    }


    @Override
    public void appendItems(AppendCartItemsRequest params) {
        this.appendItemRequests.add(params);
    }
    // validateProductQuantity
    // Not sure what the rules are so these are the ones I made up:
    // 1. if the quantity available is less than requested, allow up to that maximum available amount
    // 2. if the item is not shippable, return `null` and do not accept it as an cart item. UI should show this as unavailable elsewhere
    private ProductQuantity validateProductQuantity(ProductQuantity productQuantity) {
        // we are validating here _in the workflow_ but a debate should be had on
        // whether the inventory subdomain should do an assertion instead in the handler.
        var inv = inventoryHandlers.getInventoryStatus(
                new ProductInventoryStatusRequest(productQuantity.productId()));
        if(inv.quantityOrderable() < productQuantity.quantity()) {
            // what shall we do with an invalid/out-of-stock item?
            // 1. enqueue on a "outOfStockItems" list and update state?
            // 2. error the entire transaction?
            // 3. adjust the append request to suit available quantity? We will do that here, but customer should know
            return new ProductQuantity(productQuantity.productId(), inv.quantityOrderable());
        }
        var ship = shippingHandlers.getProductShipability(new ProductShipabilityRequest(productQuantity.productId()));
        if(!ship.isShippable()){
            return null;
        }
        return productQuantity;
    }
    private Map<String, CartItem> merge(Map<String, CartItem> ...items) {
        var result = new LinkedHashMap<String, CartItem>();
        for(var m : items) {
            for(Map.Entry<String, CartItem> e : m.entrySet()) {
                result.merge(e.getKey(), e.getValue(), (a, b) -> new CartItem(a.cartId(), a.productId(), a.quantity() + b.quantity()));
            }
        }
        return result;
    }
    private Map<String, CartItem> merge(Map<String, CartItem> items, Map<String, ProductQuantity> quantities) {
        var result = new LinkedHashMap<String, CartItem>();

        for(Map.Entry<String,ProductQuantity> e : quantities.entrySet()) {
            var current = items.get(e.getKey());
            var qty = e.getValue().quantity();

            if(current!=null) {
                qty = current.quantity()  + qty;
            }
            result.put(e.getKey(), new CartItem(Workflow.getInfo().getWorkflowId(), e.getKey(), qty));
        }

        return result;
    }
    private Map<String, ProductQuantity> processAppends(List<AppendCartItemsRequest> appends) {
        Map<String, ProductQuantity> validItems = new LinkedHashMap<>();
        // we possibly alter the list while looping so choose an iterator to do this safely.
        // new append requests might arrive while processing the list
        Iterator<AppendCartItemsRequest> appendIterator = appends.iterator();
        while(appendIterator.hasNext()) {
            AppendCartItemsRequest req = appendIterator.next();
            // this is the boundary for where asynchronous validation could happen
            // as we pick off append requests and validate them in order.
            // i would do this as an
            for(ProductQuantity q: req.productQuantities()) {

                if (validateProductQuantity(q) != null) {
                    var qty = q.quantity();
                    var current = validItems.get(q.productId());
                    if(current != null) {
                        qty = current.quantity() + qty;
                    }
                    validItems.put(q.productId(), new ProductQuantity(q.productId(), qty));
                }
            }
            appendIterator.remove();
        }
        return validItems;
    }

}
