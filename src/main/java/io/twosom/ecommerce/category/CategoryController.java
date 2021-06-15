package io.twosom.ecommerce.category;

import io.twosom.ecommerce.category.form.CategoryForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @GetMapping("/category")
    public String getAllCategories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute(categories);

        return "category/list";
    }

    @GetMapping("/category/new")
    public String createNewCategoryForm(Model model) {

        List<String> categoryTitleList = categoryRepository.findAllByParentCategoryIsNull().stream().map(Category::getTitle)
                .collect(Collectors.toList());

        model.addAttribute("categoryTitleList", categoryTitleList);
        model.addAttribute(new CategoryForm());
        return "category/new-category";
    }

    @PostMapping("/category/new")
    public String createNewCategory(CategoryForm categoryForm) {
        categoryService.createNewCategory(categoryForm);
        return "redirect:/category";
    }

}
