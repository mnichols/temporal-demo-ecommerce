package io.temporal.ecommerce.domain.inventory;
import io.temporal.ecommerce.domain.orchestrations.InventoryActivities;
import io.temporal.ecommerce.messages.queries.ProductInventoryStatusRequest;
import io.temporal.ecommerce.messages.queries.ProductInventoryStatusResponse;
import org.springframework.stereotype.Component;

@Component("inventory-handlers")
public class Handlers implements InventoryActivities {
    @Override
    public ProductInventoryStatusResponse getInventoryStatus(ProductInventoryStatusRequest req) {
        return new ProductInventoryStatusResponse(req.productId(), 0);
    }
}
