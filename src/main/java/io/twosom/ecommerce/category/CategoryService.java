package io.twosom.ecommerce.category;

import io.twosom.ecommerce.category.form.CategoryCreateForm;
import io.twosom.ecommerce.category.form.CategoryEditForm;
import io.twosom.ecommerce.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper mapper;


    public Category createNewCategory(CategoryCreateForm categoryCreateForm) {
        Category category = mapper.map(categoryCreateForm, Category.class);
        Category savedCategory = categoryRepository.save(category);

        if (StringUtils.hasText(categoryCreateForm.getParentCategoryTitle())) {
            Category parentCategory = categoryRepository.findByTitle(categoryCreateForm.getParentCategoryTitle());
            parentCategory.addChildCategory(savedCategory);
        }
        return savedCategory;
    }

    public void updateCategory(Long categoryId, CategoryEditForm categoryEditForm) {
        Category findCategory = categoryRepository.findById(categoryId).get();

        if (StringUtils.hasText(categoryEditForm.getParentCategoryTitle())) {
            if (findCategory.getParentCategory() != null) {
                findCategory.getParentCategory().removeChildCategory(findCategory);
            }

            Category parentCategory = categoryRepository.findByTitle(categoryEditForm.getParentCategoryTitle());
            parentCategory.addChildCategory(findCategory);
        }

        findCategory.updateTitleAndDescription(categoryEditForm);

    }

    public void addCategoryTitleListToModel(Model model, List<Category> categoryList) {
        List<String> categoryTitleList = categoryList.stream().map(Category::getTitle)
                .collect(Collectors.toList());

        model.addAttribute("categoryTitleList", categoryTitleList);
    }

    public void publishCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).get();
        category.setPublish(true);
    }

    public void unPublishCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).get();
        category.setPublish(false);
    }
}
