package io.twosom.ecommerce.order;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.order.form.OrderForm;
import io.twosom.ecommerce.order.repository.OrderRepository;
import io.twosom.ecommerce.shoppingbag.domain.ShoppingBag;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final ShoppingBagRepository shoppingBagRepository;

    public String createNewOrder(Account account, OrderForm orderForm, int totalSumPrice) {
        List<ShoppingBag> shoppingBagList = shoppingBagRepository.findAllByIdIn(orderForm.getIdArray());
        Address address = new Address(orderForm.getCity(), orderForm.getStreet(), orderForm.getZipcode());
        Order order = Order.createNewOrder(account, address, shoppingBagList, totalSumPrice, orderForm.getPayment());

        return orderRepository.save(order).getId();
        //TODO 판매자들에게 주문되었다고 알림 기능 만들기
    }

    public int getTotalPayedPriceByAccount(Account account) {
        return orderRepository.findAllByAccountAndStatus(account, OrderStatus.COMP)
                .stream().map(Order::getTotalSumPrice)
                .collect(Collectors.toList())
                .stream().mapToInt(i -> i).sum();
    }

    public void confirmationOrder(Order order, int savedPoint) {
        order.setStatus(OrderStatus.COMP);
        order.setSavedPoint(savedPoint);
    }
}
