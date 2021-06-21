package io.twosom.ecommerce.shoppingbag.dto;

import io.twosom.ecommerce.shoppingbag.ShoppingBagStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingBagListDto {

    private Long shoppingBagId;
    private Long productId;
    private String productName;
    private String productImage;

    private int shoppingBagQuantity;
    private int productPrice;
    private int salePrice;

    private ShoppingBagStatus status;

    private boolean sale;
    private int saleRate;

    public int getTotalPrice() {
        if (isSale()) {
            return salePrice * shoppingBagQuantity;
        } else {
            return productPrice * shoppingBagQuantity;
        }
    }


}
