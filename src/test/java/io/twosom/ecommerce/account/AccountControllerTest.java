package io.twosom.ecommerce.account;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountEventListener;
import io.twosom.ecommerce.account.event.AccountMailResendEvent;
import io.twosom.ecommerce.account.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest{

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @MockBean
    AccountEventListener accountEventListener;

    @DisplayName("회원가입 - 폼 접근")
    @Test
    void create_new_account_form() throws Exception {

        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }


    @DisplayName("회원가입 - 입력 값 오류")
    @Test
    void create_new_account_with_wrong_value() throws Exception {

        mockMvc.perform(
                post("/sign-up")
                        .param("email", "this-is-not-email")
                        .param("nickname", "srt")
                        .param("password", "1111")
                        .param("address.city", "city")
                        .param("address.street", "street")
                        .param("address.zipcode", "zipcode")
                        .param("userOrSeller", "user")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().hasErrors());
    }

    @DisplayName("회원가입 - 입력 값 정상")
    @Test
    void create_new_account_with_correct_value() throws Exception {

        mockMvc.perform(
                post("/sign-up")
                        .param("email", "test@test.com")
                        .param("nickname", "testNickname")
                        .param("password", "11111111")
                        .param("address.city", "Seoul")
                        .param("address.street", "Garosugil")
                        .param("address.zipcode", "222111")
                        .param("userOrSeller", "user")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account findAccount = accountRepository.findByNickname("testNickname");
        assertNotNull(findAccount);
        assertEquals(findAccount.getEmail(), "test@test.com");

        then(accountEventListener).should()
                .handleAccountCreatedEvent(any(AccountCreatedEvent.class));
    }


    @DisplayName("인증코드 - 인증 실패")
    @Test
    void verify_email_code_failed() throws Exception {

        create_new_account_with_correct_value();
        Account findAccount = accountRepository.findByNickname("testNickname");
        assertNotNull(findAccount);

        UserAccount userAccount = new UserAccount(findAccount);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userAccount, findAccount.getPassword(), userAccount.getAuthorities());

        mockMvc.perform(
                post("/verification-email-code")
                        .content("123123")
                        .with(authentication(token))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("인증코드 - 인증 성공")
    @Test
    void verify_email_code_success() throws Exception {

        create_new_account_with_correct_value();
        Account findAccount = accountRepository.findByNickname("testNickname");
        assertNotNull(findAccount);

        UserAccount userAccount = new UserAccount(findAccount);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userAccount, findAccount.getPassword(), userAccount.getAuthorities());

        mockMvc.perform(
                post("/verification-email-code")
                        .content(findAccount.getEmailVerificationCode())
                        .with(authentication(token))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertTrue(findAccount.isEmailVerified());

        assertNotNull(findAccount.getMemberGrade());
        assertEquals(findAccount.getMemberGrade().getGradeName(), "FAMILY");
    }

    @DisplayName("인증코드 - 이메일 재전송 성공")
    @Test
    void resend_verify_code_success() throws Exception {

        create_new_account_with_correct_value();
        Account findAccount = accountRepository.findByNickname("testNickname");
        assertNotNull(findAccount);
        String originalCode = findAccount.getEmailVerificationCode();

        assertNull(findAccount.getEmailVerificationCodeSendTime());
        UserAccount userAccount = new UserAccount(findAccount);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userAccount, findAccount.getPassword(), userAccount.getAuthorities());

        mockMvc.perform(
                post("/resend-email")
                        .content("another@email.com")
                        .with(csrf())
                        .with(authentication(token)))
                .andExpect(status().isOk());

        assertNotEquals(originalCode, findAccount.getEmailVerificationCode());
        assertNotNull(findAccount.getEmailVerificationCodeSendTime());

        then(accountEventListener).should().handleMailResendEvent(any(AccountMailResendEvent.class));
    }

    @DisplayName("인증코드 - 이메일 재전송 실패")
    @Test
    void resend_verify_code_failed() throws Exception {
        resend_verify_code_success();

        Account findAccount = accountRepository.findByNickname("testNickname");
        assertNotNull(findAccount);

        UserAccount userAccount = new UserAccount(findAccount);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userAccount, findAccount.getPassword(), userAccount.getAuthorities());

        mockMvc.perform(
                post("/resend-email")
                        .content("another@email.com")
                        .with(csrf())
                        .with(authentication(token)))
                .andExpect(status().isBadRequest());
    }


}