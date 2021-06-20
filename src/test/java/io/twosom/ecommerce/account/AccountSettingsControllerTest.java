package io.twosom.ecommerce.account;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.domain.PreviousPassword;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountEventListener;
import io.twosom.ecommerce.account.event.AccountMailResendEvent;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.account.repository.MemberGradeRepository;
import io.twosom.ecommerce.account.repository.PreviousPasswordRepository;
import io.twosom.ecommerce.account.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountSettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PreviousPasswordRepository previousPasswordRepository;

    @MockBean
    AccountEventListener accountEventListener;

    @Autowired
    MemberGradeRepository memberGradeRepository;

    @BeforeEach
    void beforeEach() {
        createNewAccount("twosom", "twosom@twosom.com", "11111111");
        createNewAccount("anotherNickname", "another@email.com", "1111111");


        then(accountEventListener)
                .should(times(2)).handleAccountCreatedEvent(any(AccountCreatedEvent.class));
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        previousPasswordRepository.deleteAll();
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


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 - 폼 접근")
    @Test
    void profile_edit_form() throws Exception {

        mockMvc.perform(get("/account/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("accountProfileEditForm", "accountPasswordEditForm"))
                .andExpect(view().name("account/edit-profile"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 - 입력 값 오류")
    @Test
    void profile_edit_with_wrong_value() throws Exception {


        mockMvc.perform(
                post("/account/settings/profile")
                        .param("email", "another@email.com")
                        .param("nickname", "another")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountPasswordEditForm"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 - 입력 값 정상")
    @Test
    void profile_edit_with_correct_value() throws Exception {

        mockMvc.perform(
                post("/account/settings/profile")
                        .param("email", "twosom2@twosom2.com")
                        .param("nickname", "twosom2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertNull(accountRepository.findByNickname("twosom"));
        assertNotNull(accountRepository.findByNickname("twosom2"));

        then(accountEventListener)
                .should().handleMailResendEvent(any(AccountMailResendEvent.class));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 - 이메일 재전송 실패로 인한 오류")
    @Test
    void profile_edit_with_email_resend_failed() throws Exception {
        profile_edit_with_correct_value();

        mockMvc.perform(
                post("/account/settings/profile")
                        .param("email", "twosom3@twosom3.com")
                        .param("nickname", "twosom3")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountPasswordEditForm"))
                .andExpect(view().name("account/edit-profile"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("비밀번호 수정 - 현재 비밀번호 오류")
    @Test
    void password_edit_with_wrong_current_password() throws Exception {

        mockMvc.perform(
                post("/account/settings/password")
                        .param("currentPassword", "this_is_not_current_password")
                        .param("newPassword", "newPassword")
                        .param("confirmNewPassword", "newPassword")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountProfileEditForm"))
                .andExpect(view().name("account/edit-profile"));

    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("비밀번호 수정 - 새 비밀번호 오류")
    @Test
    void password_edit_with_wrong_new_password() throws Exception {

        mockMvc.perform(
                post("/account/settings/password")
                        .param("currentPassword", "11111111")
                        .param("newPassword", "newPassword")
                        .param("confirmNewPassword", "notSamePassword")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("accountProfileEditForm"))
                .andExpect(view().name("account/edit-profile"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("비밀번호 수정 - 성공")
    @Test
    void password_edit_with_correct_value() throws Exception {

        mockMvc.perform(
                post("/account/settings/password")
                        .param("currentPassword", "11111111")
                        .param("newPassword", "newPassword")
                        .param("confirmNewPassword", "newPassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(view().name("redirect:/"));

        Account findAccount = accountRepository.findByNickname("twosom");

        assertTrue(passwordEncoder.matches("newPassword", findAccount.getPassword()));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("비밀번호 수정 - 이전 비밀번호 사용 - 3개월 안지난 시점")
    @Test
    void password_edit_with_previous_password_before_3months() throws Exception {

        password_edit_with_correct_value();
        mockMvc.perform(
                post("/account/settings/password")
                        .param("currentPassword", "newPassword")
                        .param("newPassword", "11111111")
                        .param("confirmNewPassword", "11111111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("비밀번호 수정 - 이전 비밀번호 사용 - 3개월 지난 시점")
    @Test
    void password_edit_with_previous_password_after_3months() throws Exception {

        Account account = accountRepository.findByNickname("twosom");
        password_edit_with_correct_value();
        List<PreviousPassword> previousPasswordList = previousPasswordRepository.findAllByAccount(account);
        PreviousPassword previousPassword = previousPasswordList.get(0);
        previousPassword.setPasswordChangedDate(LocalDateTime.now().minusMonths(4));


        mockMvc.perform(
                post("/account/settings/password")
                        .param("currentPassword", "newPassword")
                        .param("newPassword", "11111111")
                        .param("confirmNewPassword", "11111111")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(view().name("redirect:/"));

        assertTrue(passwordEncoder.matches("11111111", account.getPassword()));


    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("주소 수정 - 폼 접근")
    @Test
    void edit_address_form() throws Exception {

        mockMvc.perform(
                get("/account/settings/address"))
                .andExpect(model().attributeExists("address"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/edit-address"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("주소 수정 - 성공")
    @Test
    void edit_address_success() throws Exception {

        mockMvc.perform(
                post("/account/settings/address")
                        .param("city", "newCity")
                        .param("street", "newStreet")
                        .param("zipcode", "newZipcode")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Account findAccount = accountRepository.findByNickname("twosom");

        assertEquals(findAccount.getAddress().getCity(), "newCity");
        assertEquals(findAccount.getAddress().getStreet(), "newStreet");
        assertEquals(findAccount.getAddress().getZipcode(), "newZipcode");
    }


}