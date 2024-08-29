package io.temporal.ecommerce.messages.commands;

import io.temporal.ecommerce.messages.values.CheckoutStatus;

public record PutCheckoutRequest(String checkoutId, CheckoutStatus status) {}
