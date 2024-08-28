package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.ecommerce.messages.commands.SetCartItemsRequest;
import io.temporal.ecommerce.messages.queries.CartResponse;
import io.temporal.ecommerce.messages.workflows.InitializeCartRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Cart {
  @WorkflowMethod
  void execute(InitializeCartRequest params);

  @SignalMethod
  void setItems(SetCartItemsRequest params);

  @QueryMethod
  CartResponse getState();
}
