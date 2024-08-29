package io.temporal.ecommerce.messages.commands;

import io.temporal.ecommerce.messages.values.CartItem;

public record StartCheckoutRequest(String checkoutId, String cartId, CartItem... items) {}
