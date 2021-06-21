package io.twosom.ecommerce.notification.domain;

import io.twosom.ecommerce.account.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id @GeneratedValue
    private Long id;

    private String title;

    private String message;

    private boolean checked;

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @CreatedDate
    private LocalDateTime createdDate;
}
