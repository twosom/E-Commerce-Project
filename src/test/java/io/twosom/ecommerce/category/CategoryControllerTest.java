package io.twosom.ecommerce.category;

import io.twosom.ecommerce.account.AccountRepository;
import io.twosom.ecommerce.account.AccountService;
import io.twosom.ecommerce.account.Address;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountEventListener;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.category.form.CategoryCreateForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountEventListener accountEventListener;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryService categoryService;


    @BeforeEach
    void beforeEach() {
        createNewAccount("twosom", "twosom@twosom.com", "11111111");


        then(accountEventListener)
                .should().handleAccountCreatedEvent(any(AccountCreatedEvent.class));
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private void createNewAccount(String nickname, String email, String password) {
        SignUpForm signUpForm = SignUpForm.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .address(new Address("city", "street", "zipcode"))
                .build();

        accountService.createAccount(signUpForm);
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("카테고리 조회")
    @Test
    void category_view() throws Exception {

        mockMvc.perform(get("/admin/category"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryList"))
                .andExpect(view().name("category/list"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("새 카테고리 - 폼")
    @Test
    void create_new_category_form() throws Exception {
        mockMvc.perform(get("/admin/category/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "categoryCreateForm"))
                .andExpect(view().name("category/new-category"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("새 카테고리 추가 - 실패")
    @Test
    void create_new_category_with_wrong_value() throws Exception {

        mockMvc.perform(
                post("/admin/category/new")
                        .param("parentCategoryTitle", "")
                        .param("title", "")
                        .param("description", "new Category Description")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("category/new-category"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("카테고리 추가 - 성공")
    @Test
    void create_new_category_with_correct_value() throws Exception {

        mockMvc.perform(
                post("/admin/category/new")
                        .param("parentCategoryTitle", "")
                        .param("title", "newCategory")
                        .param("description", "new Category Description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category"));

        Category newCategory = categoryRepository.findByTitle("newCategory");
        assertNotNull(newCategory);
        assertFalse(newCategory.isPublish());
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("카테고리 추가 - 중복 된 카테고리 명 - 실패")
    @Test
    void create_new_category_with_duplicated_title() throws Exception {
        create_new_category_with_correct_value();

        mockMvc.perform(
                post("/admin/category/new")
                        .param("parentCategoryTitle", "")
                        .param("title", "newCategory")
                        .param("description", "new Category Description")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("category/new-category"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("카테고리 수정 - 폼")
    @Test
    void edit_category_form() throws Exception {

        create_new_category_with_correct_value();
        Category newCategory = categoryRepository.findByTitle("newCategory");
        assertNotNull(newCategory);


        mockMvc.perform(get("/admin/category/{id}", newCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "categoryEditForm"))
                .andExpect(view().name("category/edit-category"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("카테고리 수정 - 실패")
    @Test
    void edit_category_with_wrong_value() throws Exception {

        create_new_category_with_correct_value();
        Category newCategory = categoryRepository.findByTitle("newCategory");
        assertNotNull(newCategory);

        mockMvc.perform(
                post("/admin/category/{id}", newCategory.getId())
                        .param("parentCategoryTitle", "")
                        .param("title", "")
                        .param("description", "new Category Description")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categoryTitleList"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("카테고리 수정 - 성공")
    @Test
    void edit_category_with_correct_value() throws Exception {

        create_new_category_with_correct_value();
        Category newCategory = categoryRepository.findByTitle("newCategory");
        assertNotNull(newCategory);

        mockMvc.perform(
                post("/admin/category/{id}", newCategory.getId())
                        .param("parentCategoryTitle", "")
                        .param("title", "editedNewCategory")
                        .param("description", "new Category Description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/category"));

        String editedTitle = newCategory.getTitle();

        assertEquals(editedTitle, "editedNewCategory");
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("카테고리 수정 - 중복 된 카테고리 명 - 실패")
    @Test
    void edit_category_with_duplicated_title() throws Exception {

        create_new_category_with_correct_value();
        Category newCategory = categoryRepository.findByTitle("newCategory");
        assertNotNull(newCategory);


        /* 폼에 접근하여 Validator 객체 안에 form 을 채워주기 위한 용도. */
        mockMvc.perform(get("/admin/category/{id}", newCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "categoryEditForm"))
                .andExpect(view().name("category/edit-category"));


        createNewCategory("anotherCategory", "this is another category");

        mockMvc.perform(
                post("/admin/category/{id}", newCategory.getId())
                        .param("parentCategoryTitle", "")
                        .param("title", "anotherCategory")
                        .param("description", "new Category Description")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categoryTitleList"));
    }

    private void createNewCategory(String title, String description) {
        CategoryCreateForm categoryCreateForm = CategoryCreateForm.builder()
                .title(title)
                .description(description)
                .build();

        categoryService.createNewCategory(categoryCreateForm);
    }


}