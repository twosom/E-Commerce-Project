package io.twosom.ecommerce.shoppingbag.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingBagForm {

    private Long productId;
    private int quantity;
}
