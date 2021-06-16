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
        sendSignUpConfirmEmail(account, "E-Commerce 가입 확인 이메일입니다", account.getEmail(), "인증 번호는 " + account.getEmailVerificationCode() + " 입니다.");
    }

    @EventListener
    public void handleMailResendEvent(AccountMailResendEvent accountMailResendEvent) {
        Account account = accountMailResendEvent.getAccount();
        sendSignUpConfirmEmail(account, "E-Commerce 인증번호 재전송 안내", accountMailResendEvent.getTo(), "인증 번호는 " + account.getEmailVerificationCode() + " 입니다.");
    }

    @EventListener
    public void handleAccountResetPasswordEmailSendEvent(AccountResetPasswordEmailSendEvent accountResetPasswordEmailSendEvent) {
        Account account = accountResetPasswordEmailSendEvent.getAccount();
        sendSignUpConfirmEmail(account, "E-Commerce 비밀번호 초기화를 위한 인증번호입니다.", account.getEmail(), "인증 번호는 " + account.getEmailVerificationCode() + " 입니다.");
    }

    @EventListener
    public void handleAccountResetPasswordConfirmEvent(AccountResetPasswordConfirmEvent accountResetPasswordConfirmEvent) {
        Account account = accountResetPasswordConfirmEvent.getAccount();
        String newPassword = accountResetPasswordConfirmEvent.getNewPassword();

        sendSignUpConfirmEmail(account, "E-Commerce 비밀번호 초기화 안내", account.getEmail(), "초기화 된 비밀번호는 " + newPassword + " 입니다.");
    }


    private void sendSignUpConfirmEmail(Account account, String subject, String to, String message) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", message);
        context.setVariable("link", appProperties.getHost());

        String htmlMessage = templateEngine.process("mail/send-mail", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(to)
                .subject(subject)
                .message(htmlMessage)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
