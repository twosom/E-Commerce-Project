package io.twosom.ecommerce.account.controller;

import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.category.CategoryRepository;
import io.twosom.ecommerce.category.CategoryService;
import io.twosom.ecommerce.product.Product;
import io.twosom.ecommerce.product.ProductDto;
import io.twosom.ecommerce.product.ProductRepository;
import io.twosom.ecommerce.product.ProductService;
import io.twosom.ecommerce.product.form.ProductForm;
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
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final ProductService productService;
    private final CategoryService categoryService;

    private final ModelMapper modelMapper;


    @InitBinder("productForm")
    public void productFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new ProductFormValidator());
    }


    @GetMapping("/menu")
    public String sellerMenu() {
        return "seller/menu";
    }

    @GetMapping("/product")
    public String sellerProductList(@CurrentAccount Account account, Model model) {
        List<Product> productList = productRepository.findAllBySeller(account);
        List<Category> categoryList = productList.stream()
                .map(Product::getCategory)
                .collect(Collectors.toList());

        List<ProductDto> productDtoList = productService.convertProductListToProductDtoList(productList);
        model.addAttribute("productList", productDtoList);
        model.addAttribute("categoryList", categoryList);
        return "seller/product-list";
    }

    @GetMapping("/product/new")
    public String createProductForm(Model model) {
        categoryService.addCategoryTitleListToModel(model, categoryRepository.findAllByParentCategoryIsNotNull());
        model.addAttribute(new ProductForm());

        return "seller/new-product";
    }

    @PostMapping("/product/new")
    public String createProduct(@CurrentAccount Account account, @Valid ProductForm productForm, Errors errors, Model model) {

        if (errors.hasErrors()) {
            categoryService.addCategoryTitleListToModel(model, categoryRepository.findAllByParentCategoryIsNotNull());
            return "seller/new-product";
        }

        productService.createProduct(account, productForm);
        return "redirect:/seller/product";
    }

    @GetMapping("/product/{id}")
    public String editProductForm(@PathVariable("id") Product product, Model model) {
        ProductForm productForm = modelMapper.map(product, ProductForm.class);
        productForm.setCategoryName(product.getCategory().getTitle());

        categoryService.addCategoryTitleListToModel(model, categoryRepository.findAllByParentCategoryIsNotNull());
        model.addAttribute(productForm);

        return "seller/edit-product";
    }

    @PostMapping("/product/{id}")
    public String editProduct(@PathVariable("id") Long productId,
                              @Valid ProductForm productForm,
                              Errors errors, Model model) {

        if (errors.hasErrors()) {
            categoryService.addCategoryTitleListToModel(model, categoryRepository.findAllByParentCategoryIsNotNull());
            return "seller/edit-product";
        }

        productService.updateProduct(productId, productForm);
        return "redirect:/seller/product";
    }




    @DeleteMapping("/product/{id}")
    public String deleteProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.removeProduct(productId, account);
        return "redirect:/seller/product";
    }

    @PostMapping("/product/{id}/publish")
    public String publishProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.publishProduct(productId, account);
        return "redirect:/seller/product";
    }

    @PostMapping("/product/{id}/unpublish")
    public String unPublishProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.unPublishProduct(productId, account);
        return "redirect:/seller/product";
    }

}
