package io.twosom.ecommerce.account.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor @NoArgsConstructor
public class PreviousPassword {

    @Id @GeneratedValue
    private Long id;

    private String encodedPreviousPassword;

    @ManyToOne
    private Account account;

    @CreatedDate
    private LocalDateTime passwordChangedDate;

}
