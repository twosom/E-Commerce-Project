package io.twosom.ecommerce.product.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class SaleHistory {

    @Id @GeneratedValue
    private Long id;

    @CreatedDate
    private LocalDateTime createdDate;

    @ManyToOne
    private Product product;

    private int originalPrice;

    private int saleRate;

    private int salePrice;


}
