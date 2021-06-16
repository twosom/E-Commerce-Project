package io.twosom.ecommerce.category.validator;

import io.twosom.ecommerce.category.CategoryRepository;
import io.twosom.ecommerce.category.form.CategoryCreateForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class CategoryCreateFormValidator implements Validator {

    private final CategoryRepository categoryRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CategoryCreateForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CategoryCreateForm form = (CategoryCreateForm) target;
        if (categoryRepository.existsByTitle(form.getTitle())) {
            errors.rejectValue("title", "wrong.title", "이미 존재하는 카테고리 이름입니다.");
        }
    }
}
