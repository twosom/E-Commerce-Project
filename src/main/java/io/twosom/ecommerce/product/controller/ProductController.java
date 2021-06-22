package io.twosom.ecommerce.product.controller;

import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.category.repository.CategoryRepository;
import io.twosom.ecommerce.category.CategoryService;
import io.twosom.ecommerce.product.dto.ProductDto;
import io.twosom.ecommerce.product.dto.ProductViewDto;
import io.twosom.ecommerce.product.repository.ProductQueryRepository;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.product.service.ProductService;
import io.twosom.ecommerce.shoppingbag.ShoppingBagStatus;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
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

    private final ShoppingBagRepository shoppingBagRepository;

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    private final ModelMapper modelMapper;


    @GetMapping("/product")
    public String viewProductList(@RequestParam("category") String title, Model model) {
        if (!categoryRepository.existsByTitle(title)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }
        List<ProductDto> productList = productQueryRepository.findAllByCategoryTitleAndPublished(title);

        model.addAttribute("productList", productList);
        model.addAttribute("categoryTitle", title);

        return "product/list";
    }

    @GetMapping("/product/info")
    public String viewDetailProductInfo(@CurrentAccount Account account, @RequestParam("id") Long productId, Model model) {

        ProductViewDto productDto = productQueryRepository.findByIdAndPublished(productId);
        if (productDto == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }
        boolean shoppingBagAdded = shoppingBagRepository.existsByAccountAndProductIdAndStatus(account, productId, ShoppingBagStatus.STANDBY);
        productDto.setShoppingBagAdded(shoppingBagAdded);
        model.addAttribute("product", productDto);

        return "product/detail";
    }


}
