package io.twosom.ecommerce.product.dto;


import io.twosom.ecommerce.category.CategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {

    private Long id;
    private String productName;
    private String productImage;
    private String seller;
    private int productPrice;
    private int productStock;
    private String categoryTitle;
    private LocalDateTime createdDate;
    private boolean sale;
    private int saleRate;

    //originalPriceValue - (originalPriceValue * saleRate * 0.01
    public int getSalePrice() {
        if (isSale()) {
            return (int) (getProductPrice() - (getProductPrice() * getSaleRate() * 0.01));
        }
        return 0;
    }

}
