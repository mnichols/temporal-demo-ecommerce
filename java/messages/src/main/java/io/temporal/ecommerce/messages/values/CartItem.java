package io.temporal.ecommerce.messages.values;

public record CartItem(String cartId, String productId, Integer quantity) {}
