package io.twosom.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductIndexViewDto {

    private Long id;

    private String productName;
    private String productImage;

    private String categoryTitle;
    private int productPrice;
    private boolean sale;
    private int saleRate;
    private String sellerName;

    //originalPriceValue - (originalPriceValue * saleRate * 0.01
    public int getSalePrice() {
        if (isSale()) {
            return (int) (getProductPrice() - (getProductPrice() * getSaleRate() * 0.01));
        }
        return 0;
    }

}
