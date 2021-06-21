package io.twosom.ecommerce.notification;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;


    public boolean setToRead(Account account, Long notificationId) {
        Notification findNotification = notificationRepository.findByAccountAndId(account, notificationId);

        if (findNotification == null) {
            return false;
        }

        findNotification.setChecked(true);
        return true;
    }
}
