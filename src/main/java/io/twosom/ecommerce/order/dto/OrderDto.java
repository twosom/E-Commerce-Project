package io.twosom.ecommerce.order.dto;

import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.order.OrderStatus;
import io.twosom.ecommerce.order.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDto {

    private String orderId;

    private LocalDateTime orderedDate;

    private List<ProductDtoForOrderDto> productDtoList = new ArrayList<>();

    private int totalSumPrice;

    private int savedPoint = 0;

    private Payment payment;

    private Address address;

    private OrderStatus status;

}
