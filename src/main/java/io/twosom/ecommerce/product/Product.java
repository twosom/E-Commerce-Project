package io.twosom.ecommerce.product;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.category.Category;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Product {

    @Id @GeneratedValue
    private Long id;

    private String productName;

    @Lob
    private String productImage;

    @ManyToOne
    private Account seller;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Lob
    private String productDescription;

    private int productPrice;

    private int productStock;

    private boolean publish;

}
