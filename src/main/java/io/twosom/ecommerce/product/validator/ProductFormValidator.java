package io.twosom.ecommerce.product.validator;

import io.twosom.ecommerce.product.form.ProductForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ProductFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ProductForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProductForm form = (ProductForm) target;

        if (form.getProductPrice() < 0 ) {
            errors.rejectValue("productPrice", "error.productPrice", "가격이 0원보다 작을 순 없습니다.");
        }

        if (form.getProductStock() < 0) {
            errors.rejectValue("productStock", "error.productStock", "재고가 0개보다 작을 순 없습니다.");
        }
    }
}
