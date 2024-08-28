package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.ecommerce.messages.queries.ProductInventoryStatusRequest;
import io.temporal.ecommerce.messages.queries.ProductInventoryStatusResponse;

@ActivityInterface
public interface InventoryActivities {
  @ActivityMethod
  ProductInventoryStatusResponse getInventoryStatus(ProductInventoryStatusRequest req);
}
