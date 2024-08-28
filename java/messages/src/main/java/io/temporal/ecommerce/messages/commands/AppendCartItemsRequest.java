package io.temporal.ecommerce.messages.commands;

import io.temporal.ecommerce.messages.values.ProductQuantity;

import java.util.List;
public record AppendCartItemsRequest (String cartId, List<ProductQuantity> productQuantities){
}
