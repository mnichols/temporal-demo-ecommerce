package io.temporal.ecommerce.messages.queries;

import io.temporal.ecommerce.messages.values.CartItem;

import java.util.List;
import java.util.Map;

public record CartResponse(String id, String userId, boolean isSealed, Map<String, CartItem> items) {
}
