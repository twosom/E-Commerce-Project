package io.twosom.ecommerce.product;

import io.twosom.ecommerce.account.Account;
import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.product.form.ProductForm;
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

    @Lob
    private String productDescription;

    private int productPrice;

    private int productStock;

    private boolean publish;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
