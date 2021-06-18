package io.twosom.ecommerce.product.dto;


import io.twosom.ecommerce.category.CategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {

    private Long id;
    private String productName;
    private String productImage;
    private String productDescription;

    private String sellerName;

    private int productPrice;
    private int productStock;
    private boolean publish;
    private boolean sale;

    private CategoryDto category;

}
