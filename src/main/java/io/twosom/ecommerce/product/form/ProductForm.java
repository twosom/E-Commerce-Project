package io.twosom.ecommerce.product.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductForm {

    private Long id;

    @NotBlank
    private String categoryName;

    @NotBlank
    private String productName;

    private String productImage;

    @NotBlank
    private String productDescription;

    private int productPrice;

    private int productStock;

}
