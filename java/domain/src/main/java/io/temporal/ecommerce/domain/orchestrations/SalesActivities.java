package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.ecommerce.messages.commands.StartCheckoutRequest;

@ActivityInterface
public interface SalesActivities {
  @ActivityMethod
  void startCheckout(StartCheckoutRequest req);
}
