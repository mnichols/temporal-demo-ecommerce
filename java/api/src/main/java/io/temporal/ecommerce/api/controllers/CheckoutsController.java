package io.temporal.ecommerce.api.controllers;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.ecommerce.domain.orchestrations.Cart;
import io.temporal.ecommerce.domain.orchestrations.Checkout;
import io.temporal.ecommerce.messages.commands.PutCheckoutRequest;
import io.temporal.ecommerce.messages.queries.CheckoutResponse;
import io.temporal.failure.ApplicationFailure;
import java.net.URI;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkouts")
public class CheckoutsController {

  Logger logger = LoggerFactory.getLogger(CheckoutsController.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.application.task-queue}")
  String taskQueue;

  @GetMapping("/{id}")
  public ResponseEntity<CheckoutResponse> checkoutGet(@PathVariable("id") String id) {
    try {
      var workflowStub = temporalClient.newWorkflowStub(Checkout.class, id);
      var state = workflowStub.getState();
      return new ResponseEntity<>(state, HttpStatus.OK);
    } catch (WorkflowNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> checkoutPut(
      @PathVariable String id, @RequestBody PutCheckoutRequest params) {
    var startCart = false;
    var headers = new HttpHeaders();
    headers.setLocation(URI.create("/api/checkouts/" + id));
    try {
      var svc = this.temporalClient.getWorkflowServiceStubs();
      WorkflowExecution execution = WorkflowExecution.newBuilder().setWorkflowId(id).build();
      DescribeWorkflowExecutionResponse desc =
          svc.blockingStub()
              .describeWorkflowExecution(
                  DescribeWorkflowExecutionRequest.newBuilder()
                      .setExecution(execution)
                      .setNamespace(temporalClient.getOptions().getNamespace())
                      .build());

      if (Objects.equals(
          desc.getWorkflowExecutionInfo().getStatus(),
          WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING)) {
        return ResponseEntity.accepted().headers(headers).build();
      }
    } catch (StatusRuntimeException e) {
      startCart = Objects.equals(e.getStatus().getCode(), Status.Code.NOT_FOUND);
    } catch (WorkflowNotFoundException e) {
      startCart = true;
    }
    if (!startCart) {
      logger.error("Checkout not started");
      return ResponseEntity.internalServerError().build();
    }

    var workflowStub = temporalClient.newWorkflowStub(Cart.class, params.cartId());
    try {
      workflowStub.checkout(params);
    } catch (ApplicationFailure e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return ResponseEntity.accepted().headers(headers).build();
  }
}
