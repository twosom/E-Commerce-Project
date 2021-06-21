package io.twosom.ecommerce.order.event;

import lombok.Getter;

@Getter
public class OrderCancelledEvent {

    private final String orderId;

    public OrderCancelledEvent(String orderId) {
        this.orderId = orderId;
    }
}
