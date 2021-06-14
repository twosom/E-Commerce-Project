package io.twosom.ecommerce.account;

import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.utility.RandomString;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

    public void createVerificationCode() {
        this.emailVerificationCode = RandomString.make(6);
    }
}
