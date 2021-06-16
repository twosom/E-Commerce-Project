package io.twosom.ecommerce.product;

import io.twosom.ecommerce.account.Account;
import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.category.CategoryDto;
import io.twosom.ecommerce.category.CategoryRepository;
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
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;


    @InitBinder("productForm")
    public void productFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new ProductFormValidator());
    }


    @GetMapping("/admin/product")
    public String productList(Model model) {
        // TODO 판매자 권한을 가진 사람은 자신이 등록한 상품들만 모아보도록 해야함. 지금은 관리자 메뉴로.
        List<ProductDto> productList = productRepository.findAll()
                .stream()
                .map(product -> {
                    ProductDto productDto = modelMapper.map(product, ProductDto.class);
                    productDto.setSellerName(product.getSeller().getNickname());
                    return productDto;
                })
                .collect(Collectors.toList());

        List<CategoryDto> categoryList = categoryRepository.findAllByParentCategoryIsNotNull()
                .stream().map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toList());

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("productList", productList);

        return "product/list";
    }

    @GetMapping("/admin/product/new")
    public String createProductForm(Model model) {
        addChildCategoryTitleListToModel(model);
        model.addAttribute(new ProductForm());

        return "product/new-product";
    }

    @PostMapping("/admin/product/new")
    public String createProduct(@CurrentAccount Account account, @Valid ProductForm productForm, Errors errors, Model model) {

        if (errors.hasErrors()) {
            addChildCategoryTitleListToModel(model);
            return "product/new-product";
        }

        productService.createProduct(account, productForm);
        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/{id}")
    public String editProductForm(@PathVariable("id") Product product, Model model) {
        ProductForm productForm = modelMapper.map(product, ProductForm.class);
        productForm.setCategoryName(product.getCategory().getTitle());

        addChildCategoryTitleListToModel(model);
        model.addAttribute(productForm);

        return "product/edit-product";
    }

    @PostMapping("/admin/product/{id}")
    public String editProduct(@PathVariable("id") Long productId,
                              @Valid ProductForm productForm,
                              Errors errors, Model model) {

        if (errors.hasErrors()) {
            addChildCategoryTitleListToModel(model);
            return "product/edit-product";
        }

        productService.updateProduct(productId, productForm);
        return "redirect:/admin/product";
    }

    @DeleteMapping("/admin/product/{id}")
    public String deleteProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.removeProduct(productId, account);
        return "redirect:/admin/product";
    }

    @PostMapping("/admin/product/{id}/publish")
    public String publishProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.publishProduct(productId, account);
        return "redirect:/admin/product";
    }

    @PostMapping("/admin/product/{id}/unpublish")
    public String unPublishProduct(@PathVariable("id") Long productId, @CurrentAccount Account account) {
        productService.unPublishProduct(productId, account);
        return "redirect:/admin/product";
    }


    private void addChildCategoryTitleListToModel(Model model) {
        List<String> categoryTitleList = categoryRepository.findAllByParentCategoryIsNotNull().stream().map(Category::getTitle)
                .collect(Collectors.toList());

        model.addAttribute("categoryTitleList", categoryTitleList);
    }
}
