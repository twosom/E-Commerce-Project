package io.twosom.ecommerce.notification;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByAccountAndChecked(Account account, boolean checked);

    List<Notification> findAllByAccountAndCheckedOrderByCreatedDateDesc(Account account, boolean checked);

    Notification findByAccountAndId(Account account, Long id);

    void deleteByAccountAndChecked(Account account, boolean checked);
}
