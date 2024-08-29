package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.ecommerce.messages.commands.ApplyCartItemsChanges;
import io.temporal.ecommerce.messages.commands.PutCheckoutRequest;
import io.temporal.ecommerce.messages.queries.CartResponse;
import io.temporal.ecommerce.messages.workflows.InitializeCartRequest;
import io.temporal.workflow.*;

@WorkflowInterface
public interface Cart {
  @WorkflowMethod
  void execute(InitializeCartRequest params);

  @SignalMethod
  void applyItemsChanges(ApplyCartItemsChanges params);

  //  @UpdateValidatorMethod(updateName = "checkout")
  //  void validateCheckout(PutCheckoutRequest req);
  //
  //  @UpdateMethod
  //  void checkout(PutCheckoutRequest req);
  @SignalMethod
  void checkout(PutCheckoutRequest req);

  @QueryMethod
  CartResponse getState();
}
