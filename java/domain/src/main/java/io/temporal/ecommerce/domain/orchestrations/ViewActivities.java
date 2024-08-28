package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.ecommerce.messages.commands.WriteDenormalizedCartItemRequest;

@ActivityInterface
public interface ViewActivities {
    @ActivityMethod
    void writeDenormalizedCartItem(WriteDenormalizedCartItemRequest req);
}
