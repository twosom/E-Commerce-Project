package io.twosom.ecommerce.main;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountEventListener;
import io.twosom.ecommerce.account.event.AccountResetPasswordEmailSendEvent;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.account.repository.MemberGradeRepository;
import io.twosom.ecommerce.account.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @MockBean
    AccountEventListener accountEventListener;

    @Autowired
    MemberGradeRepository memberGradeRepository;


    @BeforeEach
    void beforeEach() {
        createNewAccount("twosom", "twosom@twosom.com", "11111111");


        then(accountEventListener)
                .should().handleAccountCreatedEvent(any(AccountCreatedEvent.class));
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    private void createNewAccount(String nickname, String email, String password) {
        SignUpForm signUpForm = SignUpForm.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .address(new Address("city", "street", "zipcode"))
                .userOrSeller("user")
                .build();

        Account account = accountService.createAccount(signUpForm);
        account.setMemberGrade(memberGradeRepository.findByGradeName("FAMILY"));
    }


    @DisplayName("메인 화면 접근 - 로그인 없이")
    @Test
    void access_index_page_with_unauthenticated() throws Exception {

        AnonymousAuthenticationToken token = new AnonymousAuthenticationToken("key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        mockMvc.perform(get("/")
                .with(authentication(token)))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeDoesNotExist("account"));

    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("메인 화면 접근 - 로그인 하고나서")
    @Test
    void access_index_page_with_authenticated() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("account"));
    }


    @DisplayName("로그인 페이지 접근")
    @Test
    void login_page() throws Exception {

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }


    @DisplayName("비밀번호 초기화 - 페이지 접근")
    @Test
    void reset_password_page() throws Exception {

        mockMvc.perform(get("/reset-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attributeExists("resetPasswordForm"));

    }

    @DisplayName("비밀번호 초기화 - 인증 코드 이메일 전송 - 실패")
    @Test
    void reset_password_send_verification_code_failed() throws Exception {

        mockMvc.perform(
                post("/reset-password/send-email")
                        .param("email", "not_exists@email.com")
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isOk())

                .andExpect(model().hasErrors())
                .andExpect(view().name("reset-password"));
    }

    @DisplayName("비밀번호 초기화 - 인증 코드 이메일 전송 - 성공")
    @Test
    void reset_password_send_verification_code_success() throws Exception {

        mockMvc.perform(
                post("/reset-password/send-email")
                        .param("email", "twosom@twosom.com")
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reset-password"))
                .andExpect(flash().attributeExists("emailSended", "emailSendedResetPasswordForm"));


        then(accountEventListener)
                .should().handleAccountResetPasswordEmailSendEvent(any(AccountResetPasswordEmailSendEvent.class));
    }

    @DisplayName("비밀번호 초기화 - 인증 코드 이메일 전송 후 코드 입력 - 실패")
    @Test
    void reset_password_after_send_verification_code_input_code_failed() throws Exception {
        reset_password_send_verification_code_success();

        mockMvc.perform(
                post("/reset-password/verification-code")
                        .param("email", "twosom@twosom.com")
                        .param("verificationCode", "NOT_CODE")
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("emailSended", "emailSendedResetPasswordForm"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("reset-password"));

    }

    @DisplayName("비밀번호 초기화 - 인증 코드 이메일 전송 후 코드 입력 - 성공")
    @Test
    void reset_password_after_send_verification_code_input_code_success() throws Exception {

        reset_password_send_verification_code_success();

        Account account = accountRepository.findByNickname("twosom");
        String originalPassword = account.getPassword();

        mockMvc.perform(
                post("/reset-password/verification-code")
                        .param("email", "twosom@twosom.com")
                        .param("verificationCode", account.getEmailVerificationCode())
                        .with(anonymous())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reset-password"))
                .andExpect(flash().attributeExists("message"));

        then(accountEventListener)
                .should().handleAccountResetPasswordEmailSendEvent(any(AccountResetPasswordEmailSendEvent.class));
        assertNotEquals(originalPassword, account.getPassword());
    }


}