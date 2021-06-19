package io.twosom.ecommerce.order.form;

import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.order.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderForm {

    private Payment payment;
    private List<Long> idArray;

    @NotBlank
    private String city;
    @NotBlank
    private String street;
    @NotBlank
    private String zipcode;
}
