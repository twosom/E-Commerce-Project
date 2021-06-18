package io.twosom.ecommerce.account.validator;

import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return SignUpForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;
        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }

        Address address = signUpForm.getAddress();

        if (!StringUtils.hasText(address.getCity())) {
            errors.rejectValue("address.city", "empty.city", "도시는 필수입니다.");
        }

        if (!StringUtils.hasText(address.getStreet())) {
            errors.rejectValue("address.street", "empty.street", "거리는 필수입니다.");
        }

        if (!StringUtils.hasText(address.getZipcode())) {
            errors.rejectValue("address.zipcode", "empty.zipcode", "우편번호는 필수입니다.");
        }

        String userOrSeller = signUpForm.getUserOrSeller();
        if (!StringUtils.hasText(userOrSeller)) {
            errors.rejectValue("userOrSeller", "empty.userOrSeller", "이 값은 필수입니다.");
        } else {
            switch (userOrSeller) {
                case "user":
                    break;
                case "seller":
                    break;
                default:
                    errors.rejectValue("userOrSeller", "wrong.userOrSeller", "잘못된 접근입니다.");
            }
        }
    }
}
