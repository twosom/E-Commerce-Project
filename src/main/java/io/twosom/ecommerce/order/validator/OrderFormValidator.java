package io.twosom.ecommerce.order.validator;

import io.twosom.ecommerce.order.form.OrderForm;
import io.twosom.ecommerce.shoppingbag.ShoppingBagStatus;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFormValidator implements Validator {

    private final ShoppingBagRepository shoppingBagRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return OrderForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OrderForm form = (OrderForm) target;

        for (Long shoppingBagId : form.getIdArray()) {
            // TODO 장바구니 안의 해당 상품이 공개 중인지 확인
            if (!shoppingBagRepository.existsByProductPublishAndId(true, shoppingBagId)) {
                errors.rejectValue("payment", "wrong.request", "잘못 된 요청입니다. 장바구니에 물건을 다시 추가해주세요.");
            }
            // TODO 장바구니 상태가 STANDBY 인지 확인
            if (!shoppingBagRepository.findById(shoppingBagId).get().getStatus().equals(ShoppingBagStatus.STANDBY)) {
                errors.rejectValue("payment", "wrong.request", "잘못 된 요청입니다. 장바구니에 물건을 다시 추가해주세요.");
            }
        }


    }
}
