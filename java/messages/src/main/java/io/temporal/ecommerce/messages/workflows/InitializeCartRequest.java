package io.temporal.ecommerce.messages.workflows;

import io.temporal.ecommerce.messages.commands.PutCheckoutRequest;
import io.temporal.ecommerce.messages.values.ProductQuantity;

public record InitializeCartRequest(
    String id, String userId, PutCheckoutRequest checkout, ProductQuantity... items) {}
