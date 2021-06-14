package io.twosom.ecommerce.account.event;

import io.twosom.ecommerce.account.Account;
import io.twosom.ecommerce.account.AccountService;
import io.twosom.ecommerce.config.AppProperties;
import io.twosom.ecommerce.mail.EmailMessage;
import io.twosom.ecommerce.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Async
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class AccountEventListener {

    private final AccountService accountService;
    private final AppProperties appProperties;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    @EventListener
    public void handleAccountCreatedEvent(AccountCreatedEvent accountCreatedEvent) {
        sendSignUpConfirmEmail(accountCreatedEvent.getAccount());
    }






    private void sendSignUpConfirmEmail(Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("emailVerificationCode", account.getEmailVerificationCode());
        context.setVariable("link", appProperties.getHost());

        String message = templateEngine.process("mail/send-mail", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("E-Commerce 가입 확인 이메일입니다")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
