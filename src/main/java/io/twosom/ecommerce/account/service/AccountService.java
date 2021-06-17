package io.twosom.ecommerce.account.service;

import io.twosom.ecommerce.account.UserAccount;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.domain.PreviousPassword;
import io.twosom.ecommerce.account.domain.Role;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountMailResendEvent;
import io.twosom.ecommerce.account.event.AccountResetPasswordConfirmEvent;
import io.twosom.ecommerce.account.event.AccountResetPasswordEmailSendEvent;
import io.twosom.ecommerce.account.form.AccountPasswordEditForm;
import io.twosom.ecommerce.account.form.AccountProfileEditForm;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.account.repository.PreviousPasswordRepository;
import io.twosom.ecommerce.main.form.ResetPasswordForm;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PreviousPasswordRepository previousPasswordRepository;

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;


    public void createAccount(SignUpForm signUpForm) {
        Account savedAccount = saveNewAccount(signUpForm);
        eventPublisher.publishEvent(new AccountCreatedEvent(savedAccount));
        login(savedAccount);
    }

    private void login(Account savedAccount) {
        UserAccount userAccount = new UserAccount(savedAccount);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userAccount, userAccount.getPassword(), userAccount.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);

        checkRole(signUpForm, account);

        account.createVerificationCode();
        return accountRepository.save(account);
    }

    private void checkRole(SignUpForm signUpForm, Account account) {
        switch (signUpForm.getUserOrSeller()) {
            case "user":
                account.setRole(Role.ROLE_USER);
                break;
            case "seller":
                account.setRole(Role.ROLE_SELLER);
                break;
            default:
                throw new RuntimeException("잘못된 접근입니다.");
        }
    }

    public boolean verifyCode(Account account, String verificationCode) {
        Account findAccount = accountRepository.findByEmail(account.getEmail());
        if (findAccount.verifyCode(verificationCode)) {
            login(findAccount);
            return true;
        }
        return false;
    }

    public boolean resendSignUpConfirmEmail(Long accountId, String email) {
        Account account = accountRepository.findById(accountId).get();
        if (account.canResendVerificationCode()) {
            account.beforeResendVerificationCode();
            eventPublisher.publishEvent(new AccountMailResendEvent(account, email));
            login(account);
            return true;
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String nicknameOrEmail) throws UsernameNotFoundException {
        Account account = accountRepository.findByNickname(nicknameOrEmail);

        if (account == null) {
            account = accountRepository.findByEmail(nicknameOrEmail);
        }

        if (account == null) {
            throw new UsernameNotFoundException(nicknameOrEmail);
        }

        return new UserAccount(account);
    }

    public void editEmailAndNickname(Long accountId, AccountProfileEditForm editForm) {
        Account account = accountRepository.findById(accountId).get();

        if (!editForm.getEmail().equals(account.getEmail())) {
            account.setEmail(editForm.getEmail());
            account.setEmailVerified(false);
            resendSignUpConfirmEmail(account.getId(), account.getEmail());
        }

        account.setNickname(editForm.getNickname());
        login(account);
    }

    public void editPassword(Long accountId, AccountPasswordEditForm editForm) {
        Account account = accountRepository.findById(accountId).get();
        if (!passwordEncoder.matches(editForm.getNewPassword(), account.getPassword())) {
            PreviousPassword previousPassword = PreviousPassword.builder()
                    .encodedPreviousPassword(account.getPassword())
                    .account(account)
                    .build();

            previousPasswordRepository.save(previousPassword);
            account.setPassword(passwordEncoder.encode(editForm.getNewPassword()));
            login(account);
        }
    }

    public void editAddress(Long accountId, Address address) {
        Account account = accountRepository.findById(accountId).get();
        account.setAddress(address);
        login(account);
    }

    public void sendResetPasswordEmail(ResetPasswordForm resetPasswordForm) {
        Account findAccount = accountRepository.findByEmail(resetPasswordForm.getEmail());
        findAccount.createVerificationCode();
        eventPublisher.publishEvent(new AccountResetPasswordEmailSendEvent(findAccount));
    }

    public void resetPassword(ResetPasswordForm resetPasswordForm) {
        Account findAccount = accountRepository.findByEmail(resetPasswordForm.getEmail());
        String newPassword = RandomString.make(10);
        findAccount.setPassword(passwordEncoder.encode(newPassword));

        eventPublisher.publishEvent(new AccountResetPasswordConfirmEvent(findAccount, newPassword));
    }
}
