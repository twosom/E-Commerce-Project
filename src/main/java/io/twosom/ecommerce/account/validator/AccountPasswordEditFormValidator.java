package io.twosom.ecommerce.account.validator;

import io.twosom.ecommerce.account.UserAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.form.AccountPasswordEditForm;
import io.twosom.ecommerce.account.service.PreviousPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class AccountPasswordEditFormValidator implements Validator {

    private final PasswordEncoder passwordEncoder;
    private final PreviousPasswordService previousPasswordService;


    @Override
    public boolean supports(Class<?> clazz) {
        return AccountPasswordEditForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserAccount userAccount = (UserAccount) authentication.getPrincipal();
        Account account = userAccount.getAccount();

        AccountPasswordEditForm form = (AccountPasswordEditForm) target;

        if (previousPasswordService.isDuplicatePassword(account, form.getNewPassword())) {
            errors.rejectValue("newPassword", "duplicate.password", "3개월동안은 이전 비밀번호를 사용하실 수 없습니다.");
        }

        if (!passwordEncoder.matches(form.getCurrentPassword(), account.getPassword())) {
            errors.rejectValue("currentPassword", "wrong.password", "잘못된 비밀번호입니다.");
        }

        if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
            errors.rejectValue("confirmNewPassword", "wrong.newPassword", "새로운 비밀번호가 서로 일치하지 않습니다.");
        }
    }
}
