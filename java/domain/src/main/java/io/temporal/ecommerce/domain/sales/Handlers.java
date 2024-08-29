package io.temporal.ecommerce.domain.sales;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import io.temporal.ecommerce.domain.orchestrations.Checkout;
import io.temporal.ecommerce.domain.orchestrations.SalesActivities;
import io.temporal.ecommerce.messages.commands.StartCheckoutRequest;
import io.temporal.serviceclient.WorkflowServiceStubs;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("sales-handlers")
public class Handlers implements SalesActivities {
  private final String taskQueue;
  Logger logger = LoggerFactory.getLogger(Handlers.class);
  private final WorkflowClient workflowClient;

  public Handlers(
      WorkflowClient workflowClient,
      @Value("${spring.temporal.workers[0].task-queue}") String taskQueue) {
    this.workflowClient = workflowClient;
    this.taskQueue = taskQueue;
  }

  @Override
  public void startCheckout(StartCheckoutRequest req) {
    var startable = false;

    try {
      WorkflowServiceStubs service = workflowClient.getWorkflowServiceStubs();
      WorkflowExecution execution =
          WorkflowExecution.newBuilder().setWorkflowId(req.checkoutId()).build();

      DescribeWorkflowExecutionResponse description =
          service
              .blockingStub()
              .describeWorkflowExecution(
                  DescribeWorkflowExecutionRequest.newBuilder()
                      .setExecution(execution)
                      .setNamespace(workflowClient.getOptions().getNamespace())
                      .build());
      if (Objects.equals(
          description.getWorkflowExecutionInfo().getStatus(),
          WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING)) {
        return;
      }
      // TODO handle case where status is not running

    } catch (StatusRuntimeException e) {
      if (Objects.equals(e.getStatus().getCode(), Status.Code.NOT_FOUND)) {
        startable = true;
      }
    } catch (WorkflowNotFoundException e) {
      startable = true;
    }
    logger.info("NOT STARTABLE");
    if (!startable) {
      return;
    }
    var wf =
        workflowClient.newWorkflowStub(
            Checkout.class,
            WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId(req.checkoutId())
                .setWorkflowIdReusePolicy(
                    WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY)
                .build());
    logger.info("starting the workflow: {}", req);
    var run =
        WorkflowClient.start(
            wf::execute,
            new io.temporal.ecommerce.messages.workflows.StartCheckoutRequest(
                req.checkoutId(), req.cartId(), req.items()));
  }
}
