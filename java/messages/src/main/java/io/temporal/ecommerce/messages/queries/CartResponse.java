package io.temporal.ecommerce.messages.queries;

import io.temporal.ecommerce.messages.commands.PutCheckoutRequest;
import io.temporal.ecommerce.messages.values.CartItem;
import java.util.Map;

public record CartResponse(
    String id,
    String userId,
    boolean isLocked,
    Map<String, CartItem> items,
    PutCheckoutRequest checkout) {}
