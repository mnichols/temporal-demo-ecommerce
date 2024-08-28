package io.temporal.ecommerce.messages.workflows;

import io.temporal.ecommerce.messages.values.CartItem;

public record InitializeCartRequest(String id, String userId, CartItem...items ) {
}
