package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.ecommerce.messages.queries.CheckoutResponse;
import io.temporal.ecommerce.messages.workflows.StartCheckoutRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Checkout {
  @WorkflowMethod
  void execute(StartCheckoutRequest params);

  @QueryMethod
  CheckoutResponse getState();
}
