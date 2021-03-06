package io.twosom.ecommerce.account.controller;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountEventListener;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.account.repository.MemberGradeRepository;
import io.twosom.ecommerce.account.service.AccountService;
import io.twosom.ecommerce.category.repository.CategoryRepository;
import io.twosom.ecommerce.category.CategoryService;
import io.twosom.ecommerce.category.form.CategoryCreateForm;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.domain.SaleHistory;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.product.repository.SaleHistoryRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SellerControllerTest {

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

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService productService;

    @Autowired
    SaleHistoryRepository saleHistoryRepository;

    @Autowired
    MemberGradeRepository memberGradeRepository;

    @BeforeEach
    void beforeEach() {
        createNewAccount("twosom", "twosom@twosom.com", "11111111", "seller");
        createNewAccount("test", "test@test.com", "11111111", "user");

        then(accountEventListener)
                .should(times(2)).handleAccountCreatedEvent(any(AccountCreatedEvent.class));

        createNewCategory("parentCategory", "", "parentDescription");
        createNewCategory("childCategory", "parentCategory", "childCategoryDescription");
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
        saleHistoryRepository.deleteAll();
        productRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private void createNewAccount(String nickname, String email, String password, String userOrSeller) {
        SignUpForm signUpForm = SignUpForm.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .address(new Address("city", "street", "zipcode"))
                .userOrSeller(userOrSeller)
                .build();

        Account account = accountService.createAccount(signUpForm);
        account.setMemberGrade(memberGradeRepository.findByGradeName("FAMILY"));

    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ?????? - ??????")
    @Test
    void access_seller_menu_success() throws Exception {

        mockMvc.perform(get("/seller/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/menu"));
    }

    @WithUserDetails(value = "test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ?????? - ??????")
    @Test
    void access_seller_menu_failed() throws Exception {

        mockMvc.perform(get("/seller/menu"))
                .andExpect(status().isForbidden());
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ?????? - ??? ??????")
    @Test
    void seller_create_new_product_form() throws Exception {
        mockMvc.perform(get("/seller/product/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "productForm"))
                .andExpect(view().name("seller/new-product"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ?????? - ????????? ??????")
    @Test
    void seller_create_new_product_with_wrong_value() throws Exception {

        mockMvc.perform(
                post("/seller/product/new")
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
                .andExpect(view().name("seller/new-product"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ?????? - ????????? ??????")
    @Test
    void seller_create_new_product_with_correct_value() throws Exception {

        mockMvc.perform(
                post("/seller/product/new")
                        .param("categoryName", "childCategory")
                        .param("productName", "testProduct")
                        .param("productImage", "testProductImage")
                        .param("productDescription", "testProductDescription")
                        .param("productPrice", "10")
                        .param("productStock", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/product"));

        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);
        assertEquals(testProduct.getSeller().getNickname(), "twosom");
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? - ??? ??????")
    @Test
    void edit_product_form() throws Exception {

        seller_create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);

        mockMvc.perform(get("/seller/product/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryTitleList", "productForm"))
                .andExpect(view().name("seller/edit-product"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? - ?????? ??? ??????")
    @Test
    void edit_product_with_wrong_value() throws Exception {

        seller_create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);


        mockMvc.perform(
                post("/seller/product/{id}", testProduct.getId())
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
                .andExpect(view().name("seller/edit-product"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? - ?????? ??? ??????")
    @Test
    void edit_product_with_correct_value() throws Exception {

        seller_create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);


        mockMvc.perform(
                post("/seller/product/{id}", testProduct.getId())
                        .param("categoryName", "childCategory")
                        .param("productName", "newTestProduct")
                        .param("productImage", "newTestImage")
                        .param("productDescription", "newTestProductDescription")
                        .param("productPrice", "100")
                        .param("productStock", "100")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/product"));

        assertEquals(testProduct.getProductName(), "newTestProduct");

    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ??????")
    @Test
    void remove_product() throws Exception {
        seller_create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);

        mockMvc.perform(
                delete("/seller/product/{id}", testProduct.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/product"));

        assertNull(productRepository.findByProductName("testProduct"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ??????")
    @Test
    void publish_product() throws Exception {

        seller_create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);

        assertFalse(testProduct.isPublish());

        mockMvc.perform(
                post("/seller/product/{id}/publish", testProduct.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/product"));


        assertTrue(testProduct.isPublish());
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????????")
    @Test
    void unPublish_product() throws Exception {

        publish_product();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);
        assertTrue(testProduct.isPublish());


        mockMvc.perform(
                post("/seller/product/{id}/unpublish", testProduct.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/product"));

        assertFalse(testProduct.isPublish());
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? - ?????? ??? ??????")
    @Test
    void sale_product_with_correct_value() throws Exception {

        seller_create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);


        mockMvc.perform(
                post("/seller/product/{id}", testProduct.getId())
                        .param("categoryName", "childCategory")
                        .param("productName", "newTestProduct")
                        .param("productImage", "newTestImage")
                        .param("productDescription", "newTestProductDescription")
                        .param("productPrice", "100")
                        .param("productStock", "100")
                        .param("saleRate", "50")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/product"));

        assertEquals(testProduct.getProductName(), "newTestProduct");
        assertTrue(testProduct.isSale());
        List<SaleHistory> saleHistoryList = saleHistoryRepository.findAll();
        assertEquals(saleHistoryList.size(), 1);
        SaleHistory saleHistory = saleHistoryList.get(0);

        assertEquals(saleHistory.getProduct(), testProduct);
        assertEquals(saleHistory.getOriginalPrice(), testProduct.getProductPrice());
        assertEquals(saleHistory.getSalePrice(), testProduct.calculateSalePrice());
        assertEquals(saleHistory.getSaleRate(), testProduct.getSaleRate());

    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? - ?????? ??? ??????")
    @Test
    void sale_product_with_wrong_value() throws Exception {

        seller_create_new_product_with_correct_value();
        Product testProduct = productRepository.findByProductName("testProduct");
        assertNotNull(testProduct);


        mockMvc.perform(
                post("/seller/product/{id}", testProduct.getId())
                        .param("categoryName", "childCategory")
                        .param("productName", "newTestProduct")
                        .param("productImage", "newTestImage")
                        .param("productDescription", "newTestProductDescription")
                        .param("productPrice", "100")
                        .param("productStock", "100")
                        .param("saleRate", "-10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("seller/edit-product"));

    }


}