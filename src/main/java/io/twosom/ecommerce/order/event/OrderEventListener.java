package io.twosom.ecommerce.order.event;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.notification.domain.Notification;
import io.twosom.ecommerce.notification.NotificationRepository;
import io.twosom.ecommerce.notification.domain.NotificationType;
import io.twosom.ecommerce.order.repository.OrderQueryRepository;
import io.twosom.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;

@Async
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationRepository notificationRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderRepository orderRepository;


    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) {
        createNotificationToSeller(orderCreatedEvent.getOrderId(), NotificationType.ORDER_CREATED, "상품 구매 안내", "주문 생성");
    }



    @EventListener
    public void handleOrderConfirmedEvent(OrderConfirmedEvent orderConfirmedEvent) {
        createNotificationToSeller(orderConfirmedEvent.getOrderId(), NotificationType.ORDER_CONFIRMED, "구매 확정 안내", "구매 확정");
    }

    @EventListener
    public void handlerOrderCancelledEvent(OrderCancelledEvent orderCancelledEvent) {
        createNotificationToSeller(orderCancelledEvent.getOrderId(), NotificationType.ORDER_CANCELLED, "주문 취소 안내", "주문 확정");
    }


    private void createNotificationToSeller(String orderId, NotificationType orderCreated, String title, String message) {
        Map<Account, List<String>> sellerAndProductNameFromOrderById = orderQueryRepository.findSellerAndProductFromOrderById(orderId);

        for (Map.Entry<Account, List<String>> accountListEntry : sellerAndProductNameFromOrderById.entrySet()) {
            String addCommaStr = StringUtils.join(accountListEntry.getValue(), ',');

            createNotification(accountListEntry, addCommaStr, orderCreated, title, message);
        }
    }






    private void createNotification(Map.Entry<Account, List<String>> accountListEntry, String productTitleList, NotificationType notificationType, String title, String message) {
        Notification notification = Notification.builder()
                .account(accountListEntry.getKey())
                .title(title)
                .message(productTitleList + "에 해당하는 상품에 " + message + " 이벤트가 발생하였습니다.")
                .notificationType(notificationType)
                .build();

        notificationRepository.save(notification);
    }
}
