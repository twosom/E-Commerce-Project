package io.twosom.ecommerce.account.controller;

import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.category.CategoryDto;
import io.twosom.ecommerce.category.CategoryRepository;
import io.twosom.ecommerce.category.CategoryService;
import io.twosom.ecommerce.category.form.CategoryCreateForm;
import io.twosom.ecommerce.category.form.CategoryEditForm;
import io.twosom.ecommerce.category.validator.CategoryCreateFormValidator;
import io.twosom.ecommerce.category.validator.CategoryEditFormValidator;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.dto.ProductDto;
import io.twosom.ecommerce.product.form.ProductForm;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.product.service.ProductService;
import io.twosom.ecommerce.product.validator.ProductFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductRepository productRepository;
    private final ProductService productService;

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final ModelMapper modelMapper;

    private final CategoryCreateFormValidator categoryCreateFormValidator;
    private final CategoryEditFormValidator categoryEditFormValidator;


    @InitBinder("productForm")
    public void productFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new ProductFormValidator());
    }

    @InitBinder("categoryCreateForm")
    public void categoryCreateFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(categoryCreateFormValidator);
    }

    @InitBinder("categoryEditForm")
    public void categoryEditFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(categoryEditFormValidator);
    }

    @GetMapping("/menu")
    public String adminMenu() {

        return "admin/menu";
    }


    @GetMapping("/product")
    public String productList(Model model) {
        // TODO 그냥 QueryDSL 사용하기
        List<ProductDto> productList = productService.convertProductListToProductDtoList(productRepository.findAll());

        List<CategoryDto> categoryList = categoryRepository.findAllByParentCategoryIsNotNull()
                .stream().map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toList());

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("productList", productList);

        return "admin/product/list";
    }

    @GetMapping("/product/new")
    public String createProductForm(Model model) {
        categoryService.addCategoryTitleListToModel(model, categoryRepository.findAllByParentCategoryIsNotNull());
        model.addAttribute(new ProductForm());

        return "admin/product/new-product";
    }

    @PostMapping("/product/new")
    public String createProduct(@CurrentAccount Account account, @Valid ProductForm productForm, Errors errors, Model model) {

        if (errors.hasErrors()) {
            categoryService.addCategoryTitleListToModel(model, categoryRepository.findAllByParentCategoryIsNotNull());
            return "admin/product/new-product";
        }

        productService.createProduct(account, productForm);
        return "redirect:/admin/product";
    }

    @GetMapping("/product/{id}")
    public String editProductForm(@PathVariable("id") Product product, Model model) {
        ProductForm productForm = modelMapper.map(product, ProductForm.class);
        productForm.setCategoryName(product.getCategory().getTitle());

        categoryService.addCategoryTitleListToModel(model, categoryRepository.findAllByParentCategoryIsNotNull());
        model.addAttribute(productForm);

        return "admin/product/edit-product";
    }

    @PostMapping("/product/{id}")
    public String editProduct(@PathVariable("id") Long productId,
                              @Valid ProductForm productForm,
                              Errors errors, Model model) {

        if (errors.hasErrors()) {
            categoryService.addCategoryTitleListToModel(model, categoryRepository.findAllByParentCategoryIsNotNull());
            return "admin/product/edit-product";
        }

        productService.updateProduct(productId, productForm);
        return "redirect:/admin/product";
    }

    @DeleteMapping("/product/{id}")
    public String deleteProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.removeProduct(productId, account);
        return "redirect:/admin/product";
    }

    @PostMapping("/product/{id}/publish")
    public String publishProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.publishProduct(productId, account);
        return "redirect:/admin/product";
    }

    @PostMapping("/product/{id}/unpublish")
    public String unPublishProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.unPublishProduct(productId, account);
        return "redirect:/admin/product";
    }

    //== ADMIN_CATEGORY ==//

    @GetMapping("/category")
    public String getAllCategories(Model model) {
        List<CategoryDto> categoryList = categoryRepository.findAll()
                .stream().map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toList());

        model.addAttribute("categoryList", categoryList);

        return "admin/category/list";
    }

    @GetMapping("/category/new")
    public String createNewCategoryForm(Model model) {

        addCategoryTitleListToModel(model);
        model.addAttribute(new CategoryCreateForm());
        return "admin/category/new-category";
    }

    @PostMapping("/category/new")
    public String createNewCategory(@Valid CategoryCreateForm categoryCreateForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            addCategoryTitleListToModel(model);
            return "admin/category/new-category";
        }
        categoryService.createNewCategory(categoryCreateForm);
        return "redirect:/admin/category";
    }

    @GetMapping("/category/{id}")
    public String editCategoryForm(@PathVariable("id") Category category, Model model) {

        CategoryEditForm categoryEditForm = modelMapper.map(category, CategoryEditForm.class);

        if (category.getParentCategory() != null) {
            categoryEditForm.setParentCategoryTitle(category.getParentCategory().getTitle());
        }

        categoryEditFormValidator.addForm(categoryEditForm);
        addCategoryTitleListToModel(model);
        model.addAttribute(categoryEditForm);

        return "admin/category/edit-category";
    }

    @PostMapping("/category/{id}")
    public String editCategory(@PathVariable("id") Long categoryId, @Valid CategoryEditForm categoryEditForm, Errors errors, Model model) {

        if (errors.hasErrors()) {
            addCategoryTitleListToModel(model);
            return "admin/category/edit-category";
        }

        categoryService.updateCategory(categoryId, categoryEditForm);
        return "redirect:/admin/category";
    }

    @PostMapping("/category/{id}/publish")
    public String publishCategory(@PathVariable("id") Long id) {
        categoryService.publishCategory(id);
        return "redirect:/admin/category";
    }

    @PostMapping("/category/{id}/unpublish")
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
