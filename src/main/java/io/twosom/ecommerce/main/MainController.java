package io.twosom.ecommerce.main;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.service.AccountService;
import io.twosom.ecommerce.account.CurrentAccount;
import io.twosom.ecommerce.main.form.ResetPasswordForm;
import io.twosom.ecommerce.main.validator.ResetPasswordFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ResetPasswordFormValidator resetPasswordFormValidator;
    private final AccountService accountService;

    @InitBinder("resetPasswordForm")
    public void resetPasswordFormInitBind(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(resetPasswordFormValidator);
    }


    @GetMapping("/")
    public String index(@CurrentAccount Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(Model model) {
        model.addAttribute(new ResetPasswordForm());
        return "reset-password";
    }

    @PostMapping("/reset-password/send-email")
    public String resetPasswordSendEmail(@Valid ResetPasswordForm resetPasswordForm, Errors errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            return "reset-password";
        }

        accountService.sendResetPasswordEmail(resetPasswordForm);
        redirectAttributes.addFlashAttribute("emailSended", true);
        redirectAttributes.addFlashAttribute("emailSendedResetPasswordForm",resetPasswordForm);

        return "redirect:/reset-password";
    }

    @PostMapping("/reset-password/verification-code")
    public String resetPasswordVerificationCode(@Valid ResetPasswordForm resetPasswordForm, Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("emailSended", true);
            model.addAttribute("emailSendedResetPasswordForm", resetPasswordForm);
            return "reset-password";
        }

        accountService.resetPassword(resetPasswordForm);
        redirectAttributes.addFlashAttribute("message", "비밀번호 초기화를 완료하였습니다. 메일을 확인해주세요.");
        return "redirect:/reset-password";
    }
}

