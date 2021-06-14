package io.twosom.ecommerce.account;

import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountMailResendEvent;
import io.twosom.ecommerce.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;


    public void createAccount(SignUpForm signUpForm) {
        Account savedAccount = saveNewAccount(signUpForm);
        eventPublisher.publishEvent(new AccountCreatedEvent(savedAccount));
        login(savedAccount);
    }

    private void login(Account savedAccount) {
        UserAccount userAccount = new UserAccount(savedAccount);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userAccount, userAccount.getPassword(), userAccount.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.createVerificationCode();
        return accountRepository.save(account);
    }

    public boolean verifyCode(Account account, String verificationCode) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        if (findAccount.verifyCode(verificationCode)) {
            login(findAccount);
            return true;
        }
        return false;
    }

    public boolean resendSignUpConfirmEmail(Long accountId, String email) {
        Account account = accountRepository.findById(accountId).get();
        if (account.canResendVerificationCode()) {
            account.beforeResendVerificationCode();
            eventPublisher.publishEvent(new AccountMailResendEvent(account, email));
            login(account);
            return true;
        }
        return false;
    }
}
