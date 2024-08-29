package io.temporal.ecommerce.messages.commands;

public record SetShippingDetailsRequest(String address1, String address2, String postalAddress) {
}
