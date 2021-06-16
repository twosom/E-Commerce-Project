package io.twosom.ecommerce.account.event;

import io.twosom.ecommerce.account.Account;
import lombok.Getter;

@Getter
public class AccountResetPasswordConfirmEvent {

    private final Account account;
    private final String newPassword;

    public AccountResetPasswordConfirmEvent(Account account, String newPassword) {
        this.account = account;
        this.newPassword = newPassword;
    }
}
