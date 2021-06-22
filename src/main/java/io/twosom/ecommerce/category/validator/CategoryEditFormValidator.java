package io.twosom.ecommerce.category.validator;

import io.twosom.ecommerce.category.repository.CategoryRepository;
import io.twosom.ecommerce.category.form.CategoryEditForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class CategoryEditFormValidator implements Validator {

    private final CategoryRepository categoryRepository;

    private CategoryEditForm originalForm;

    @Override
    public boolean supports(Class<?> clazz) {
        return CategoryEditForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (originalForm == null) {
            return;
        }

        CategoryEditForm editForm = (CategoryEditForm) target;

        if (!editForm.getTitle().equals(originalForm.getTitle())) {

            if (categoryRepository.existsByTitle(editForm.getTitle())) {
                errors.rejectValue("title", "wrong.title", "이미 존재하는 카테고리 이름입니다.");
            }

        }

        originalForm = null;
    }


    public void addForm(CategoryEditForm categoryEditForm) {
        this.originalForm = categoryEditForm;
    }
}
