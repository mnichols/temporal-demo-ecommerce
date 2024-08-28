package io.temporal.ecommerce.messages.commands;

import io.temporal.ecommerce.messages.values.CartItem;

public record WriteDenormalizedCartItemRequest(String userId, CartItem item) {}
