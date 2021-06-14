package io.twosom.ecommerce.account;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserAccount extends User {

    private final Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
