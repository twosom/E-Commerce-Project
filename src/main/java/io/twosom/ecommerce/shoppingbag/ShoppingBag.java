package io.twosom.ecommerce.shoppingbag;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.product.domain.Product;
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
}
