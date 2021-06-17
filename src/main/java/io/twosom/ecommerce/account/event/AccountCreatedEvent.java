package io.twosom.ecommerce.account.event;

import io.twosom.ecommerce.account.domain.Account;
import lombok.Getter;

@Getter
public class AccountCreatedEvent {

    private final Account account;


    public AccountCreatedEvent(Account account) {
        this.account = account;
    }
}
