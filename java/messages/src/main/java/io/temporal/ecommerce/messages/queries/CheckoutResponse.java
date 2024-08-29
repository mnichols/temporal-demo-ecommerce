package io.temporal.ecommerce.messages.queries;

import io.temporal.ecommerce.messages.values.CheckoutStatus;

public record CheckoutResponse(String id, String cartId, CheckoutStatus status) {}
