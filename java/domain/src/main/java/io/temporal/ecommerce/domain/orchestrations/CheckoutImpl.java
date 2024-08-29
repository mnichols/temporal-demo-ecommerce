package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.ecommerce.messages.commands.SetPaymentDetailsRequest;
import io.temporal.ecommerce.messages.commands.SetShippingDetailsRequest;
import io.temporal.ecommerce.messages.queries.CheckoutResponse;
import io.temporal.ecommerce.messages.values.CheckoutStatus;
import io.temporal.ecommerce.messages.workflows.StartCheckoutRequest;
import io.temporal.workflow.Workflow;

public class CheckoutImpl implements Checkout {
  private CheckoutResponse state;
  private SetShippingDetailsRequest pendingShippingDetails;
  private SetPaymentDetailsRequest pendingPaymentDetails;

  @Override
  public void execute(StartCheckoutRequest params) {
    this.state = new CheckoutResponse(params.checkoutId(), params.cartId(), CheckoutStatus.STARTED, false, null);

    Workflow.await(()->false /*pausing for demo */);
    while(!this.state.isReadyForPayment()) {
        Workflow.await(() -> this.pendingShippingDetails != null || this.pendingPaymentDetails != null);
        // asynchronously handle each of these details handler pipelines, nullifying after complete
      // update the state with the side effects of handling the shipping and payment details
      this.state = new CheckoutResponse(this.state.id(), this.state.cartId(), this.state.status(), true, null);
    }
    // call billingHandlers.makePayment(...) activity


  }

  @Override
  public CheckoutResponse getState() {
    return this.state;
  }

  @Override
  public void setShippingDetails(SetShippingDetailsRequest req) {
    this.pendingShippingDetails = req;
  }

  @Override
  public void setPaymentDetails(SetPaymentDetailsRequest req) {
    this.pendingPaymentDetails = req;
  }
}
