package io.twosom.ecommerce.order;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum Payment {

    DEPOSIT("무통장 입금"), CREDIT_CARD("카드 결제");

    private final String description;
}

