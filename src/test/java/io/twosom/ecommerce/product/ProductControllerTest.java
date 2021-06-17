package io.twosom.ecommerce.product;

import io.twosom.ecommerce.account.AccountRepository;
import io.twosom.ecommerce.account.AccountService;
import io.twosom.ecommerce.account.Address;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountEventListener;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.category.CategoryRepository;
import io.twosom.ecommerce.category.CategoryService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

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
                .build();

        accountService.createAccount(signUpForm);
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
    @DisplayName("상품 전체 목록")
    @Test
    void all_products_list() throws Exception {

        mockMvc.perform(get("/admin/product"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryList", "productList"))
                .andExpect(view().name("product/list"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("상품 등록 - 폼 접근")
    @Test
    void create_new_product_form() throws Exception {

        mockMvc.perform(get("/admin/product/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "productForm"))
                .andExpect(view().name("product/new-product"));
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
                .andExpect(view().name("product/new-product"));
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
                .andExpect(view().name("product/edit-product"));
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
                .andExpect(view().name("product/edit-product"));
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


}