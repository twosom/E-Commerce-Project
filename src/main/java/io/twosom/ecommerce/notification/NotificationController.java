package io.twosom.ecommerce.notification;

import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notificationList = notificationRepository.findAllByAccountAndCheckedOrderByCreatedDateDesc(account, false);
        long countOfChecked = notificationRepository.countByAccountAndChecked(account, true);
        divideNotificationsByTypeAndAddToModel(model, notificationList, notificationList.size(), countOfChecked);
        model.addAttribute("isNew", true);

        return "notification/list";
    }


    @GetMapping("/notifications/old")
    public String oldNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notificationList = notificationRepository.findAllByAccountAndCheckedOrderByCreatedDateDesc(account, true);
        long countOfNotChecked = notificationRepository.countByAccountAndChecked(account, false);

        divideNotificationsByTypeAndAddToModel(model, notificationList, countOfNotChecked, notificationList.size());
        model.addAttribute("isNew", false);

        return "notification/list";
    }


    @PostMapping("/notifications")
    @ResponseBody
    public ResponseEntity readNotification(@CurrentAccount Account account, @RequestBody Long notificationId) {
        if (notificationService.setToRead(account, notificationId)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/notifications")
    public String deleteCheckedNotifications(@CurrentAccount Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
        return "redirect:/notifications";
    }



    private void divideNotificationsByTypeAndAddToModel(Model model, List<Notification> notificationList, long countOfNotChecked, long countOfChecked) {
        List<Notification> orderCreatedNotificationList = new ArrayList<>();
        List<Notification> orderConfirmedNotificationList = new ArrayList<>();
        List<Notification> orderCancelledNotificationList = new ArrayList<>();

        for (Notification notification : notificationList) {
            switch (notification.getNotificationType()) {
                case ORDER_CREATED:
                    orderCreatedNotificationList.add(notification);
                    break;
                case ORDER_CONFIRMED:
                    orderConfirmedNotificationList.add(notification);
                    break;
                case ORDER_CANCELLED:
                    orderCancelledNotificationList.add(notification);
                    break;
            }
        }

        model.addAttribute("countOfNotChecked", countOfNotChecked);
        model.addAttribute("countOfChecked", countOfChecked);
        model.addAttribute("orderCreatedNotificationList", orderCreatedNotificationList);
        model.addAttribute("orderConfirmedNotificationList", orderConfirmedNotificationList);
        model.addAttribute("orderCancelledNotificationList", orderCancelledNotificationList);
        model.addAttribute("notificationList", notificationList);
    }

}
