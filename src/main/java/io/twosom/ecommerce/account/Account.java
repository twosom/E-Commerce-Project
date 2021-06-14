package io.twosom.ecommerce.account;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Account {

    @Id @GeneratedValue
    private Long id;

    private String email;

    private String nickname;

    private String password;

    private String emailVerified;

    private String emailVerificationCode;

    private LocalDateTime joinedAt;

    @Embedded
    private Address address;
}
