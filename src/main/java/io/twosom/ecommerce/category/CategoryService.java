package io.twosom.ecommerce.category;

import io.twosom.ecommerce.category.form.CategoryForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper mapper;


    public void createNewCategory(CategoryForm categoryForm) {
        Category category = mapper.map(categoryForm, Category.class);
        Category savedCategory = categoryRepository.save(category);

        if (StringUtils.hasText(categoryForm.getParentCategoryTitle())) {
            Category parentCategory = categoryRepository.findByTitle(categoryForm.getParentCategoryTitle());
            parentCategory.addChildCategory(savedCategory);
        }
    }
}
