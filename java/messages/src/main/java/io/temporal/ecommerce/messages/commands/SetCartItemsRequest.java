package io.temporal.ecommerce.messages.commands;

import io.temporal.ecommerce.messages.values.ProductQuantity;
import java.util.List;

public record SetCartItemsRequest(String cartId, List<ProductQuantity> productQuantities) {}
