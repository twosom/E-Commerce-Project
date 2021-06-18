package io.twosom.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductViewDto {

    private Long id;
    private String productName;
    private String productImage;
    private String seller;
    private String categoryTitle;
    private LocalDateTime createdDate;
    private String productDescription;

    private int productPrice;
    private int productStock;

    private boolean shoppingBagAdded;

    private boolean sale;
    private int saleRate;


    //originalPriceValue - (originalPriceValue * saleRate * 0.01
    public Double getSalePrice() {
        if (isSale()) {
            return getProductPrice() - (getProductPrice() * getSaleRate() * 0.01);
        }
        return null;
    }
}
