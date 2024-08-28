package io.temporal.ecommerce.messages.queries;

public record ProductInventoryStatusResponse(String productId, Integer quantityOrderable) {}
