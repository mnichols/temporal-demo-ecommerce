package io.temporal.ecommerce.messages.queries;

public record ProductShipabilityResponse(String productId, boolean isShippable) {}
