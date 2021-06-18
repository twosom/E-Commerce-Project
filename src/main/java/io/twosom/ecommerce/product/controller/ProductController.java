package io.twosom.ecommerce.product.controller;

import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.category.CategoryRepository;
import io.twosom.ecommerce.category.CategoryService;
import io.twosom.ecommerce.product.dto.ProductViewDto;
import io.twosom.ecommerce.product.repository.ProductQueryRepository;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;
    private final ProductService productService;

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    private final ModelMapper modelMapper;


    @GetMapping("/product")
    public String viewProductList(@RequestParam("category") String title, Model model) {
        Category category = categoryRepository.findByTitleAndPublish(title, true);
        if (category == null) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }
        List<ProductViewDto> productList = productQueryRepository.findAllByCategoryAndPublished(category);

        model.addAttribute("productList", productList);
        model.addAttribute("categoryTitle", title);

        return "product/list";
    }

    @GetMapping("/product/info")
    public String viewDetailProductInfo(@RequestParam("id") Long id, Model model) {

        ProductViewDto productDto = productQueryRepository.findByIdAndPublished(id);
        if (productDto == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }

        model.addAttribute("product", productDto);

        return "product/detail";
    }



}
