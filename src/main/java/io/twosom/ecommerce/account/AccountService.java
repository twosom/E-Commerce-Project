package io.twosom.ecommerce.account;

import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.config.AppProperties;
import io.twosom.ecommerce.mail.EmailMessage;
import io.twosom.ecommerce.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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
//        sendSignUpConfirmEmail(savedAccount);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));

        Account account = modelMapper.map(signUpForm, Account.class);
        account.createVerificationCode();
        return accountRepository.save(account);
    }
}
