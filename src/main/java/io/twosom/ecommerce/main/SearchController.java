package io.twosom.ecommerce.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.repository.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final ProductQueryRepository productQueryRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/search/fragment/{keyword}")
    @ResponseBody
    public ResponseEntity autoCompleteByKeyword(@PathVariable("keyword") String keyword) throws JsonProcessingException {
        List<String> allKeywords = productQueryRepository.getAllProductNamesByKeyword(keyword);
        return ResponseEntity.ok(objectMapper.writeValueAsString(allKeywords));
    }

    @GetMapping("/search")
    public String searchProduct(@RequestParam("productName") String productName) {
        String[] splitProductName = productName.split(" - ");
        Long id = productQueryRepository.getProductIdByProductName(splitProductName[0]);
        return "redirect:/product/info?id=" + id;
    }

    @GetMapping("/search/keyword")
    public String searchByKeyword(@RequestParam("keyword") String keyword, Model model) {
        List<Product> productList = productQueryRepository.getAllProductByKeyword(keyword);
        model.addAttribute("productList", productList);

        return "search/result";
    }
}
