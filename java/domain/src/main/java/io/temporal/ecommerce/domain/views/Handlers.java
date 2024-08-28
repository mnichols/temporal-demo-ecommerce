package io.temporal.ecommerce.domain.views;

import io.temporal.ecommerce.domain.orchestrations.ViewActivities;
import io.temporal.ecommerce.messages.values.CartItem;
import org.springframework.stereotype.Component;

@Component("view-handlers")
public class Handlers implements ViewActivities {
    @Override
    public void writeDenormalizedCartItem(CartItem cartItem) {

    }
}
