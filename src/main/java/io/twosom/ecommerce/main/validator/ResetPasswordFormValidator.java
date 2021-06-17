package io.twosom.ecommerce.main.validator;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.main.form.ResetPasswordForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ResetPasswordFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return ResetPasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ResetPasswordForm form = (ResetPasswordForm) target;

        if (!accountRepository.existsByEmail(form.getEmail())) {
            errors.rejectValue("email", "wrong.email", "잘못된 이메일입니다.");
        }

        if (form.getVerificationCode() != null) {
            Account findAccount = accountRepository.findByEmail(form.getEmail());
            if (!findAccount.verifyCode(form.getVerificationCode())) {
                errors.rejectValue("verificationCode", "wrong.verificationCode", "인증 코드가 일치하지 않습니다.");
            }
        }



    }
}
