package io.twosom.ecommerce.account.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.utility.RandomString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    private String email;

    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailVerificationCode;

    private LocalDateTime emailVerificationCodeSendTime;

    private LocalDateTime joinedAt;

    @ManyToOne
    private MemberGrade memberGrade;

    @Enumerated(EnumType.STRING)
    private Role role;

    private int point = 0;

    @Embedded
    private Address address;

    public void createVerificationCode() {
        this.emailVerificationCode = RandomString.make(6);
    }

    public void beforeResendVerificationCode() {
        createVerificationCode();
        this.emailVerificationCodeSendTime = LocalDateTime.now();
    }

    public boolean verifyCode(String verificationCode) {
        if (getEmailVerificationCode().equals(verificationCode)) {
            this.emailVerified = true;
            this.joinedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public boolean canResendVerificationCode() {
        return this.emailVerificationCodeSendTime == null
                || this.emailVerificationCodeSendTime.isBefore(LocalDateTime.now().minusHours(1));
    }
}
