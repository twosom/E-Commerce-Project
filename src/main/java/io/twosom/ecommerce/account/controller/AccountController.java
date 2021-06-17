package io.twosom.ecommerce.account.controller;

import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.account.service.AccountService;
import io.twosom.ecommerce.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;


    @InitBinder("signUpForm")
    public void signUpFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }


    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        accountService.createAccount(signUpForm);
        return "redirect:/";
    }

    @ResponseBody
    @PostMapping("/verification-email-code")
    public ResponseEntity verificationEmailCode(@CurrentAccount Account account,
                                                @RequestBody String verificationCode) {

        if (accountService.verifyCode(account, verificationCode)) {
            return ResponseEntity.ok(
                    "인증에 성공했습니다. " + accountRepository.countByEmailVerified(true) + "번째 회원, " + account.getNickname() + "님 가입을 축하합니다."
            );
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/resend-email")
    @ResponseBody
    public ResponseEntity resendVerificationEmailCode(@CurrentAccount Account account,
                                                      @RequestBody String email) {
        boolean result = accountService.resendSignUpConfirmEmail(account.getId(), email);

        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }


}
