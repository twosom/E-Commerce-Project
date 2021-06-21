package io.twosom.ecommerce.order.event;

import lombok.Getter;

@Getter
public class OrderConfirmedEvent {

    private final String orderId;

    public OrderConfirmedEvent(String orderId) {
        this.orderId = orderId;
    }
}
