package io.twosom.ecommerce.account.validator;

import io.twosom.ecommerce.account.Account;
import io.twosom.ecommerce.account.AccountRepository;
import io.twosom.ecommerce.account.UserAccount;
import io.twosom.ecommerce.account.form.AccountProfileEditForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class AccountProfileEditFormValidator implements Validator {

    private final AccountRepository accountRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return AccountProfileEditForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = userAccount.getAccount();
        AccountProfileEditForm form = (AccountProfileEditForm) target;

        if (form.getEmail().equals(account.getEmail()) && form.getNickname().equals(account.getNickname())) {
            return;
        }


        if (accountRepository.existsByEmail(form.getEmail())) {
            if (form.getEmail().equals(account.getEmail())) {
                return;
            }
            errors.rejectValue("email", "wrong.email", "이미 사용중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(form.getNickname())) {
            if (form.getNickname().equals(account.getNickname())) {
                validateCanEmailResend(errors, account);
                return;
            }
            errors.rejectValue("nickname", "wrong.nickname", "이미 사용중인 닉네임입니다.");
        }

        validateCanEmailResend(errors, account);

    }

    private void validateCanEmailResend(Errors errors, Account account) {
        if (!account.canResendVerificationCode()) {
            errors.rejectValue("email", "wrong.mailSend", "아직 이메일 인증 코드를 보낼 수 없으므로 이메일 변경 작업을 완료할 수 없습니다.");
        }
    }
}
