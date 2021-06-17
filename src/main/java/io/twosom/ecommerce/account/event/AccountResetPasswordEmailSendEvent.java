package io.twosom.ecommerce.account.event;

import io.twosom.ecommerce.account.domain.Account;
import lombok.Getter;

@Getter
public class AccountResetPasswordEmailSendEvent {

    private final Account account;

    public AccountResetPasswordEmailSendEvent(Account account) {
        this.account = account;
    }
}
