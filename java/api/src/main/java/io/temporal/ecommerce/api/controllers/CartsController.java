package io.temporal.ecommerce.api.controllers;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import io.temporal.ecommerce.domain.orchestrations.Cart;
import io.temporal.ecommerce.messages.queries.CartResponse;
import io.temporal.ecommerce.messages.workflows.InitializeCartRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartsController {

  Logger logger = LoggerFactory.getLogger(CartsController.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.application.task-queue}")
  String taskQueue;

  @GetMapping("/{id}")
  public ResponseEntity<CartResponse> cartGet(@PathVariable("id") String id) {
    try {
      var workflowStub = temporalClient.newWorkflowStub(Cart.class, id);
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
  ResponseEntity<String> cartPut(
      @PathVariable String id, @RequestBody InitializeCartRequest params) {

    return startWorkflow(id, params);
  }

  private ResponseEntity<String> startWorkflow(String id, InitializeCartRequest params) {
    // for now we ignore the passed in cartItems, but later we use these to initialize a Cart with items already
    // included.
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .build();
    var workflowStub = temporalClient.newWorkflowStub(Cart.class, options);

    var wfArgs = params;
    //        var wfArgs = new InitializeCartRequest(params.id(), params.value());
    // Start the workflow execution.
    try {
      var run = WorkflowClient.start(workflowStub::execute, wfArgs);
      var headers = new HttpHeaders();
      headers.setLocation(URI.create(String.format("/api/carts/%s", id)));
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (WorkflowExecutionAlreadyStarted was) {
      logger.info("Workflow execution already started: {}", id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
