package io.temporal.ecommerce.domain.inventory;

import io.temporal.ecommerce.domain.orchestrations.InventoryActivities;
import io.temporal.ecommerce.messages.queries.ProductInventoryStatusRequest;
import io.temporal.ecommerce.messages.queries.ProductInventoryStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("inventory-handlers")
public class Handlers implements InventoryActivities {
  Logger logger = LoggerFactory.getLogger(Handlers.class);

  @Override
  public ProductInventoryStatusResponse getInventoryStatus(ProductInventoryStatusRequest req) {
    var qty = 100;
    // somewhere in the productId is `inv_` so this works: "blahblah_inv__1"
    if (req.productId().contains("inv__")) {
      logger.info("evaluating {}", req.productId());
      var parts = req.productId().split("__", 0);
      for (var i = 0; i < parts.length; i++) {
        if (parts[i].contains("inv")) {
          qty = Integer.parseInt(parts[i + 1]);
          logger.info("assigning qty to {}", qty);
        }
      }
    }
    return new ProductInventoryStatusResponse(req.productId(), qty);
  }
}
