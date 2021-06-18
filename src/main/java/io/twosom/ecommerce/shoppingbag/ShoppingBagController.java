package io.twosom.ecommerce.shoppingbag;

import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.shoppingbag.dto.ShoppingBagListDto;
import io.twosom.ecommerce.shoppingbag.exception.ProductStockNotEnoughException;
import io.twosom.ecommerce.shoppingbag.form.ShoppingBagForm;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagQueryRepository;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class ShoppingBagController {

    private final ShoppingBagService shoppingBagService;
    private final ShoppingBagRepository shoppingBagRepository;
    private final ShoppingBagQueryRepository shoppingBagQueryRepository;


    @PostMapping("/shopping-bag/add")
    public ResponseEntity addShoppingBag(@CurrentAccount Account account, @RequestBody ShoppingBagForm shoppingBagForm) {
        try {
            shoppingBagService.add(account, shoppingBagForm);
        } catch (IllegalArgumentException | ProductStockNotEnoughException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/shopping-bag/minus")
    public ResponseEntity minusShoppingBag(@CurrentAccount Account account, @RequestBody ShoppingBagForm shoppingBagForm) {
        try {
            shoppingBagService.minus(account, shoppingBagForm);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/shopping-bag/list")
    public String shoppingBagList(@CurrentAccount Account account, Model model) {
        List<ShoppingBagListDto> shoppingBagList = shoppingBagQueryRepository.findByAccountAndStatusToDto(account, ShoppingBagStatus.STANDBY);
        List<Integer> totalPriceList = shoppingBagList.stream().map(ShoppingBagListDto::getTotalPrice)
                .collect(Collectors.toList());

        int totalPrice = totalPriceList.stream().mapToInt(i -> i).sum();


        model.addAttribute("shoppingBagList", shoppingBagList);
        model.addAttribute("totalPrice", totalPrice);

        return "shopping-bag/list";
    }


}
