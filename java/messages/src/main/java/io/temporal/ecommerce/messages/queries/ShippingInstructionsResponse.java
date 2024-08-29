package io.temporal.ecommerce.messages.queries;

public record ShippingInstructionsResponse(String address1, String address2, String postalCode) {
}
