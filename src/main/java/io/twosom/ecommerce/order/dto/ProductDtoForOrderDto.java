package io.twosom.ecommerce.order.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDtoForOrderDto {

    private Long id;
    private String productName;
    private String productImage;
    private String sellerName;

    private int productPrice;
    private int quantity;
    private String orderId;

    public int getTotalPrice() {
        return productPrice * quantity;
    }
}
