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
        Account account = accountCreatedEvent.getAccount();
        sendSignUpConfirmEmail(account, "E-Commerce 가입 확인 이메일입니다", account.getEmail());
    }

    @EventListener
    public void handleMailResendEvent(AccountMailResendEvent accountMailResendEvent) {
        Account account = accountMailResendEvent.getAccount();
        sendSignUpConfirmEmail(account, "E-Commerce 인증번호 재전송 안내", accountMailResendEvent.getTo());
    }


    private void sendSignUpConfirmEmail(Account account, String subject, String to) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("emailVerificationCode", account.getEmailVerificationCode());
        context.setVariable("link", appProperties.getHost());

        String message = templateEngine.process("mail/send-mail", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(to)
                .subject(subject)
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
