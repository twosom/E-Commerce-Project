package io.twosom.ecommerce.order.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter @Setter
public class ShoppingBagIdArrayForm {
    private List<Long> idArray;

    public ShoppingBagIdArrayForm(List<Long> idArray) {
        this.idArray = idArray;
    }


}
