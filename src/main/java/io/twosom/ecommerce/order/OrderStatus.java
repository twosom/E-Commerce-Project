package io.twosom.ecommerce.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {
    READY("주문 준비"), COMP("주문 확정"), CANCEL("주문 취소");

    private final String description;
}
