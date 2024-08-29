package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.ecommerce.messages.queries.CheckoutResponse;
import io.temporal.ecommerce.messages.workflows.StartCheckoutRequest;
import io.temporal.workflow.Workflow;

public class CheckoutImpl implements Checkout {
  private CheckoutResponse state;

  @Override
  public void execute(StartCheckoutRequest params) {

    Workflow.await(() -> false);
  }

  @Override
  public CheckoutResponse getState() {
    return this.state;
  }
}
