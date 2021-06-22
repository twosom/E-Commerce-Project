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
public class ProductDtoForAdminAndSeller {

    private Long id;
    private String productName;
    private int productPrice;
    private int productStock;
    private int sellCount;
    private String sellerName;
    private boolean publish;
    private boolean sale;
    private Long categoryId;

}
