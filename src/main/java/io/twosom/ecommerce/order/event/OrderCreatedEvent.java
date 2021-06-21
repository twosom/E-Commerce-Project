package io.twosom.ecommerce.order.event;

import lombok.Getter;

@Getter
public class OrderCreatedEvent {

    private final String orderId;

    public OrderCreatedEvent(String orderId) {
        this.orderId = orderId;
    }
}
