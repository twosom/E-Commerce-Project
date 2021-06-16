package io.twosom.ecommerce.category;

import io.twosom.ecommerce.category.form.CategoryCreateForm;
import io.twosom.ecommerce.category.form.CategoryEditForm;
import io.twosom.ecommerce.category.validator.CategoryCreateFormValidator;
import io.twosom.ecommerce.category.validator.CategoryEditFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final ModelMapper modelMapper;

    private final CategoryCreateFormValidator categoryCreateFormValidator;
    private final CategoryEditFormValidator categoryEditFormValidator;

    @InitBinder("categoryCreateForm")
    public void categoryCreateFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(categoryCreateFormValidator);
    }

    @InitBinder("categoryEditForm")
    public void categoryEditFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(categoryEditFormValidator);
    }


    @GetMapping("/admin/category")
    public String getAllCategories(Model model) {
        List<CategoryDto> categories = categoryRepository.findAll()
                .stream().map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toList());

        model.addAttribute("categoryList", categories);

        return "category/list";
    }

    @GetMapping("/admin/category/new")
    public String createNewCategoryForm(Model model) {

        addCategoryTitleListToModel(model);
        model.addAttribute(new CategoryCreateForm());
        return "category/new-category";
    }

    @PostMapping("/admin/category/new")
    public String createNewCategory(@Valid CategoryCreateForm categoryCreateForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            addCategoryTitleListToModel(model);
            return "category/new-category";
        }
        categoryService.createNewCategory(categoryCreateForm);
        return "redirect:/admin/category";
    }

    @GetMapping("/admin/category/{id}")
    public String editCategoryForm(@PathVariable("id") Category category, Model model) {

        CategoryEditForm categoryEditForm = modelMapper.map(category, CategoryEditForm.class);

        if (category.getParentCategory() != null) {
            categoryEditForm.setParentCategoryTitle(category.getParentCategory().getTitle());
        }

        categoryEditFormValidator.addForm(categoryEditForm);
        addCategoryTitleListToModel(model);
        model.addAttribute(categoryEditForm);

        return "category/edit-category";
    }

    @PostMapping("/admin/category/{id}")
    public String editCategory(@PathVariable("id") Long categoryId, @Valid CategoryEditForm categoryEditForm, Errors errors, Model model) {

        if (errors.hasErrors()) {
            addCategoryTitleListToModel(model);
            return "category/edit-category";
        }

        categoryService.updateCategory(categoryId, categoryEditForm);
        return "redirect:/admin/category";
    }

    @PostMapping("/admin/category/{id}/publish")
    public String publishCategory(@PathVariable("id") Long id) {
        categoryService.publishCategory(id);
        return "redirect:/admin/category";
    }

    @PostMapping("/admin/category/{id}/unpublish")
    public String unPublishCategory(@PathVariable("id") Long id) {
        categoryService.unPublishCategory(id);
        return "redirect:/admin/category";
    }

    private void addCategoryTitleListToModel(Model model) {
        List<String> categoryTitleList = categoryRepository.findAllByParentCategoryIsNull().stream().map(Category::getTitle)
                .collect(Collectors.toList());

        model.addAttribute("categoryTitleList", categoryTitleList);
    }


}
