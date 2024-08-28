package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.ecommerce.messages.values.CartItem;

@ActivityInterface
public interface ViewActivities {
    @ActivityMethod
    void writeDenormalizedCartItem(CartItem cartItem);
}
