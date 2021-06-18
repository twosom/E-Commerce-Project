package io.twosom.ecommerce.product.domain;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.category.Category;
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
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    private String productName;

    @Lob
    private String productImage;

    @ManyToOne
    private Account seller;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @CreatedDate
    private LocalDateTime createdDate;

    @Lob
    private String productDescription;

    private int productPrice;

    private int productStock;

    private boolean publish;

    private boolean sale;

    private int saleRate;

    private int salePrice;

    //originalPriceValue - (originalPriceValue * saleRate * 0.01
    public int calculateSalePrice() {
        return (int) (getProductPrice() - (getProductPrice() * getSaleRate() * 0.01));
    }

}
