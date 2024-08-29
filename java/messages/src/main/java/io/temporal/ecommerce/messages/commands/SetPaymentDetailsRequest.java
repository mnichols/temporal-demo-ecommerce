package io.temporal.ecommerce.messages.commands;

public record SetPaymentDetailsRequest(String paymentToken, Integer amountCents) {
}
