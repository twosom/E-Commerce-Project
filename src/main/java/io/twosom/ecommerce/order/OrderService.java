package io.twosom.ecommerce.order;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.notification.NotificationRepository;
import io.twosom.ecommerce.order.event.OrderCancelledEvent;
import io.twosom.ecommerce.order.event.OrderConfirmedEvent;
import io.twosom.ecommerce.order.event.OrderCreatedEvent;
import io.twosom.ecommerce.order.form.OrderForm;
import io.twosom.ecommerce.order.repository.OrderRepository;
import io.twosom.ecommerce.shoppingbag.domain.ShoppingBag;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher applicationEventPublisher;

    public String createNewOrder(Account account, OrderForm orderForm, int totalSumPrice) {
        List<ShoppingBag> shoppingBagList = shoppingBagRepository.findAllByIdIn(orderForm.getIdArray());
        Address address = new Address(orderForm.getCity(), orderForm.getStreet(), orderForm.getZipcode());
        Order order = Order.createNewOrder(account, address, shoppingBagList, totalSumPrice, orderForm.getPayment());

        return orderRepository.save(order).getId();
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

        applicationEventPublisher.publishEvent(new OrderConfirmedEvent(order.getId()));
    }

    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId).get();
        if (order == null) {
            throw new RuntimeException("존재하지 않는 주문입니다.");
        }
        order.setStatus(OrderStatus.CANCEL);
        applicationEventPublisher.publishEvent(new OrderCancelledEvent(order.getId()));
    }
}
