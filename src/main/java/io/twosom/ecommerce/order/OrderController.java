package io.twosom.ecommerce.order;

import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.order.dto.OrderDto;
import io.twosom.ecommerce.order.form.OrderForm;
import io.twosom.ecommerce.order.form.ShoppingBagIdArrayForm;
import io.twosom.ecommerce.order.repository.OrderQueryRepository;
import io.twosom.ecommerce.order.repository.OrderRepository;
import io.twosom.ecommerce.order.validator.OrderFormValidator;
import io.twosom.ecommerce.shoppingbag.ShoppingBagService;
import io.twosom.ecommerce.shoppingbag.dto.ShoppingBagListDto;
import io.twosom.ecommerce.shoppingbag.form.ShoppingBagForm;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagQueryRepository;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderService orderService;

    private final ShoppingBagRepository shoppingBagRepository;
    private final ShoppingBagQueryRepository shoppingBagQueryRepository;
    private final ShoppingBagService shoppingBagService;

    private final OrderFormValidator orderFormValidator;

    @InitBinder("orderForm")
    public void orderFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(orderFormValidator);
    }

    /* 장바구니를 통한 구매 */
    @GetMapping("/order")
    public String orderItem(@CurrentAccount Account account, ShoppingBagIdArrayForm shoppingBagIdArrayForm, Model model) {
        System.out.println("OrderController.orderItem");
        List<ShoppingBagListDto> shoppingBagList = shoppingBagQueryRepository.findAllByIdIn(shoppingBagIdArrayForm.getIdArray());
        int totalSumPrice = getTotalSumPrice(shoppingBagList);


        model.addAttribute("address", account.getAddress());
        model.addAttribute("shoppingBagList", shoppingBagList);
        model.addAttribute(new OrderForm());
        model.addAttribute("totalSumPrice", totalSumPrice);
        return "order/order-form";
    }

    /* 장바구니를 통한 구매 */
    @PostMapping("/order")
    public String confirmOrder(@CurrentAccount Account account,
                               @Valid OrderForm orderForm,
                               Errors errors,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        List<ShoppingBagListDto> shoppingBagList = shoppingBagQueryRepository.findAllByIdIn(orderForm.getIdArray());
        int totalSumPrice = getTotalSumPrice(shoppingBagList);
        if (errors.hasErrors()) {
            model.addAttribute("shoppingBagList", shoppingBagList);
            model.addAttribute("address", account.getAddress());
            model.addAttribute("totalSumPrice", totalSumPrice);
            return "order/order-form";
        }

        String savedOrderId = orderService.createNewOrder(account, orderForm, totalSumPrice);

        //TODO 주문 완료 시 주문 목록들로 리다이렉트 "/order/list" 주문 완료 상태일 때는 salePrice 컬럼이 비어있지 않으면 salePrice 보여주기
        //TODO RedirectAttribute 에 OrderDto 넣기
        OrderDto orderDtoForModal = orderQueryRepository.getOrderDtoForModalById(savedOrderId);
        redirectAttributes.addFlashAttribute("orderDtoForModal", orderDtoForModal);
        return "redirect:/order/list";
    }


    @GetMapping("/order/list")
    public String orderList(@CurrentAccount Account account, Model model) {
        List<OrderDto> orderList = orderQueryRepository.findAllByAccount(account);
        divideOrderListByStatusAndAddToModel(model, orderList);

        return "order/list";
    }

    /* 장바구니를 통해서가 아닌 직접 구매 */
    @PostMapping("/order/direct")
    public String orderWithoutShoppingBagAdd(@CurrentAccount Account account,
                                             @RequestParam("productId") Long productId) {
        ShoppingBagForm shoppingBagForm = ShoppingBagForm.builder()
                .productId(productId)
                .quantity(1)
                .build();
        Long shoppingBagId = shoppingBagService.add(account, shoppingBagForm);
        return "redirect:/order?idArray=" + shoppingBagId;
    }

    @PostMapping("/order/confirmation")
    public String confirmationOrder(@RequestParam("orderId") String orderId) {
        Order order = orderRepository.findById(orderId).get();
        if (order == null) {
            throw new RuntimeException("존재하지 않는 주문입니다.");
        }
        order.setStatus(OrderStatus.COMP);
        //TODO 판매자에게 알림 보내기
        return "redirect:/order/list";
    }

    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam("orderId") String orderId) {
        Order order = orderRepository.findById(orderId).get();
        if (order == null) {
            throw new RuntimeException("존재하지 않는 주문입니다.");
        }
        order.setStatus(OrderStatus.CANCEL);
        //TODO 판매자에게 알림 보내기
        return "redirect:/order/list";
    }


    private void divideOrderListByStatusAndAddToModel(Model model, List<OrderDto> orderList) {
        ArrayList<OrderDto> orderReadyList = new ArrayList<>();
        ArrayList<OrderDto> orderCompList = new ArrayList<>();
        ArrayList<OrderDto> orderCancelList = new ArrayList<>();

        for (OrderDto orderDto : orderList) {
            switch (orderDto.getStatus()) {
                case READY:
                    orderReadyList.add(orderDto);
                    break;
                case COMP:
                    orderCompList.add(orderDto);
                    break;
                case CANCEL:
                    orderCancelList.add(orderDto);
                    break;
            }
        }
        model.addAttribute("orderReadyList", orderReadyList);
        model.addAttribute("orderCompList", orderCompList);
        model.addAttribute("orderCancelList", orderCancelList);
    }

    private int getTotalSumPrice(List<ShoppingBagListDto> shoppingBagList) {
        return shoppingBagList.stream()
                .map(ShoppingBagListDto::getTotalPrice)
                .collect(Collectors.toList()).
                        stream().mapToInt(i -> i).sum();
    }


}
