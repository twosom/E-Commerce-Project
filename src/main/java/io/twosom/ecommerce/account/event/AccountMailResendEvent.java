package io.twosom.ecommerce.account.event;

import io.twosom.ecommerce.account.Account;
import lombok.Getter;

@Getter
public class AccountMailResendEvent {

    private final Account account;
    private final String to;

    public AccountMailResendEvent(Account account, String to) {
        this.account = account;
        this.to = to;
    }
}
