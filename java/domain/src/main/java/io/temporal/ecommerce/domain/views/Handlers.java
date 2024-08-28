package io.temporal.ecommerce.domain.views;

import io.temporal.ecommerce.domain.orchestrations.ViewActivities;
import io.temporal.ecommerce.messages.commands.WriteDenormalizedCartItemRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("view-handlers")
public class Handlers implements ViewActivities {
  Logger logger = LoggerFactory.getLogger(Handlers.class);

  @Override
  public void writeDenormalizedCartItem(WriteDenormalizedCartItemRequest req) {
    logger.info("writing denormalized cart item: {}", req);
  }
}
