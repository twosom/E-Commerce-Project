package io.twosom.ecommerce.account;

import io.twosom.ecommerce.account.form.AccountPasswordEditForm;
import io.twosom.ecommerce.account.form.AccountProfileEditForm;
import io.twosom.ecommerce.account.validator.AccountPasswordEditFormValidator;
import io.twosom.ecommerce.account.validator.AccountProfileEditFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account/settings")
public class AccountSettingsController {

    private final AccountService accountService;

    private final ModelMapper modelMapper;
    private final AccountPasswordEditFormValidator passwordEditFormValidator;
    private final AccountProfileEditFormValidator accountProfileEditFormValidator;

    @InitBinder("accountPasswordEditForm")
    public void passwordInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordEditFormValidator);
    }

    @InitBinder("accountProfileEditForm")
    public void profileInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(accountProfileEditFormValidator);
    }


    @GetMapping("/profile")
    public String editProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(modelMapper.map(account, AccountProfileEditForm.class));
        model.addAttribute(new AccountPasswordEditForm());
        return "account/edit-profile";
    }

    @PostMapping("/profile")
    public String editProfile(@CurrentAccount Account account, @Valid AccountProfileEditForm editForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(new AccountPasswordEditForm());
            return "account/edit-profile";
        }

        accountService.editEmailAndNickname(account.getId(), editForm);

        return "redirect:/";
    }

    @PostMapping("/password")
    public String editPassword(@CurrentAccount Account account, @Valid AccountPasswordEditForm editForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(modelMapper.map(account, AccountProfileEditForm.class));
            return "account/edit-profile";
        }

        accountService.editPassword(account.getId(), editForm);

        return "redirect:/";
    }

    @GetMapping("/address")
    public String editAddressForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account.getAddress());
        return "account/edit-address";
    }

    @PostMapping("/address")
    public String editAddress(@CurrentAccount Account account, Address address) {
        accountService.editAddress(account.getId(), address);
        return "redirect:/";
    }

}
