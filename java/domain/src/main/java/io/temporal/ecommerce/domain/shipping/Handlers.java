package io.temporal.ecommerce.domain.shipping;

import io.temporal.ecommerce.domain.orchestrations.ShippingActivities;
import io.temporal.ecommerce.messages.queries.ProductShipabilityRequest;
import io.temporal.ecommerce.messages.queries.ProductShipabilityResponse;
import org.springframework.stereotype.Component;

@Component("shipping-handlers")
public class Handlers implements ShippingActivities {

    @Override
    public ProductShipabilityResponse getProductShipability(ProductShipabilityRequest req) {
        return new ProductShipabilityResponse(req.productId(), true);
    }
}
