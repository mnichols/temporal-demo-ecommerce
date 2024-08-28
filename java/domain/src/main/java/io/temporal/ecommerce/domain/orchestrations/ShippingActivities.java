package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.ecommerce.messages.queries.ProductShipabilityRequest;
import io.temporal.ecommerce.messages.queries.ProductShipabilityResponse;

@ActivityInterface
public interface ShippingActivities {
    @ActivityMethod
    public ProductShipabilityResponse getProductShipability(ProductShipabilityRequest req);
}
