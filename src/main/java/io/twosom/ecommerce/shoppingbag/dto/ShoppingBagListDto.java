package io.twosom.ecommerce.shoppingbag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingBagListDto {

    private Long productId;
    private String productName;
    private String productImage;

    private int shoppingBagQuantity;
    private int productPrice;

}
