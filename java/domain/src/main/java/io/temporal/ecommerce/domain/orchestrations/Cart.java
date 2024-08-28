package io.temporal.ecommerce.domain.orchestrations;

import io.temporal.ecommerce.messages.api.InitializeCartRequest;
import io.temporal.ecommerce.messages.commands.AppendCartItemsRequest;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Cart {
    @WorkflowMethod
    void execute(InitializeCartRequest params);

    @UpdateMethod
    void appendItems(AppendCartItemsRequest params);

}
