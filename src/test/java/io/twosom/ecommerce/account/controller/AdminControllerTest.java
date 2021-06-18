package io.twosom.ecommerce.account.controller;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.domain.Role;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountEventListener;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.account.service.AccountService;
import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.category.CategoryRepository;
import io.twosom.ecommerce.category.CategoryService;
import io.twosom.ecommerce.category.form.CategoryCreateForm;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.product.service.ProductService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @MockBean
    AccountEventListener accountEventListener;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService productService;

    @BeforeEach
    void beforeEach() {
        createNewAccount("twosom", "twosom@twosom.com", "11111111");


        then(accountEventListener)
                .should().handleAccountCreatedEvent(any(AccountCreatedEvent.class));

        createNewCategory("parentCategory", "", "parentDescription");
        createNewCategory("childCategory", "parentCategory", "childCategoryDescription");
    }

    private void createNewAccount(String nickname, String email, String password) {
        SignUpForm signUpForm = SignUpForm.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .address(new Address("city", "street", "zipcode"))
                .userOrSeller("user")
                .build();

        accountService.createAccount(signUpForm);
        changeToAdmin(nickname);
    }

    private void changeToAdmin(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        account.setRole(Role.ROLE_ADMIN);
    }

    private void createNewCategory(String title, String parentCategoryTitle, String description) {
        CategoryCreateForm form = CategoryCreateForm.builder()
                .title(title)
                .parentCategoryTitle(parentCategoryTitle)
                .description(description)
                .build();

        categoryService.createNewCategory(form);
    }

    @AfterEach
    void afterEach() {
        productRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("관리자 메뉴 진입 - 성공")
    @Test
    void access_admin_menu_success() throws Exception {

        mockMvc.perform(get("/admin/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/menu"));

    }

    @DisplayName("관리자 메뉴 진입 - 실패")
    @Test
    void access_admin_menu_failed() throws Exception {

        mockMvc.perform(get("/admin/menu"))
                .andExpect(status().isForbidden());
        
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 전체 목록")
    @Test
    void all_products_list() throws Exception {

        mockMvc.perform(get("/admin/product"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryList", "productList"))
                .andExpect(view().name("admin/product/list"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 등록 - 폼 접근")
    @Test
    void create_new_product_form() throws Exception {

        mockMvc.perform(get("/admin/product/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "productForm"))
                .andExpect(view().name("admin/product/new-product"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 등록 - 입력값 오류")
    @Test
    void create_new_product_with_wrong_value() throws Exception {

        mockMvc.perform(
                post("/admin/product/new")
                        .param("categoryName", "childCategory")
                        .param("productName", "testProduct")
                        .param("productImage", "testProductImage")
                        .param("productDescription", "testProductDescription")
                        .param("productPrice", "-1")
                        .param("productStock", "-1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categoryTitleList"))
                .andExpect(view().name("admin/product/new-product"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 등록 - 입력값 정상")
    @Test
    void create_new_product_with_correct_value() throws Exception {

        mockMvc.perform(
                post("/admin/product/new")
                        .param("categoryName", "childCategory")
                        .param("productName", "testProduct")
                        .param("productImage", "testProductImage")
                        .param("productDescription", "testProductDescription")
                        .param("productPrice", "10")
                        .param("productStock", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));

        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);
        assertEquals(testProduct.getSeller().getNickname(), "twosom");
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 수정 - 폼 접근")
    @Test
    void edit_product_form() throws Exception {

        create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);

        mockMvc.perform(get("/admin/product/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "productForm"))
                .andExpect(view().name("admin/product/edit-product"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 수정 - 입력 값 오류")
    @Test
    void edit_product_with_wrong_value() throws Exception {

        create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);


        mockMvc.perform(
                post("/admin/product/{id}", testProduct.getId())
                        .param("categoryName", "childCategory")
                        .param("productName", "testProduct")
                        .param("productImage", "testProductImage")
                        .param("productDescription", "testProductDescription")
                        .param("productPrice", "-1")
                        .param("productStock", "-1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categoryTitleList"))
                .andExpect(view().name("admin/product/edit-product"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 수정 - 입력 값 정상")
    @Test
    void edit_product_with_correct_value() throws Exception {

        create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);


        mockMvc.perform(
                post("/admin/product/{id}", testProduct.getId())
                        .param("categoryName", "childCategory")
                        .param("productName", "newTestProduct")
                        .param("productImage", "newTestImage")
                        .param("productDescription", "newTestProductDescription")
                        .param("productPrice", "100")
                        .param("productStock", "100")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));

        assertEquals(testProduct.getProductName(), "newTestProduct");

    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 삭제")
    @Test
    void remove_product() throws Exception {
        create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);

        mockMvc.perform(
                delete("/admin/product/{id}", testProduct.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));

        assertNull(productRepository.findByProductName("testProduct"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 공개")
    @Test
    void publish_product() throws Exception {

        create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);

        assertFalse(testProduct.isPublish());

        mockMvc.perform(
                post("/admin/product/{id}/publish", testProduct.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));


        assertTrue(testProduct.isPublish());
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 비공개")
    @Test
    void unPublish_product() throws Exception {

        publish_product();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);
        assertTrue(testProduct.isPublish());


        mockMvc.perform(
                post("/admin/product/{id}/unpublish", testProduct.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));


        assertFalse(testProduct.isPublish());
    }



    //== CATEGORY_TEST ==//
    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("카테고리 조회")
    @Test
    void category_view() throws Exception {

        mockMvc.perform(get("/admin/category"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryList"))
                .andExpect(view().name("admin/category/list"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("새 카테고리 - 폼")
    @Test
    void create_new_category_form() throws Exception {
        mockMvc.perform(get("/admin/category/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "categoryCreateForm"))
                .andExpect(view().name("admin/category/new-category"));
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
                .andExpect(view().name("admin/category/new-category"));
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
                .andExpect(view().name("admin/category/new-category"));
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
                .andExpect(view().name("admin/category/edit-category"));
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
                .andExpect(view().name("admin/category/edit-category"));


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