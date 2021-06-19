package io.twosom.ecommerce.shoppingbag.domain;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.order.Order;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.shoppingbag.ShoppingBagStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
@Getter @Setter
public class ShoppingBag {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Account account;

    @ManyToOne
    private Product product;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private ShoppingBagStatus status;

    @CreatedDate
    private LocalDateTime createdDate;

    //TODO 주문이 완료된 상태에는 최종 할인된 가격으로 표기하기.
    private int salePrice;

    @ManyToOne
    private Order order;
}
