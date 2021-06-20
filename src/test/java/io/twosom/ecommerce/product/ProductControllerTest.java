package io.twosom.ecommerce.product;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.domain.Role;
import io.twosom.ecommerce.account.event.AccountCreatedEvent;
import io.twosom.ecommerce.account.event.AccountEventListener;
import io.twosom.ecommerce.account.form.SignUpForm;
import io.twosom.ecommerce.account.repository.AccountRepository;
import io.twosom.ecommerce.account.repository.MemberGradeRepository;
import io.twosom.ecommerce.account.service.AccountService;
import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.category.CategoryRepository;
import io.twosom.ecommerce.category.CategoryService;
import io.twosom.ecommerce.category.form.CategoryCreateForm;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.form.ProductForm;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    MemberGradeRepository memberGradeRepository;


    @BeforeEach
    void beforeEach() {
        Account account = createNewAccount("twosom", "twosom@twosom.com", "11111111");
        then(accountEventListener)
                .should().handleAccountCreatedEvent(any(AccountCreatedEvent.class));

        //== CREATE_NEW_CATEGORY ==//
        createNewCategory("parentCategory", "", "parentDescription");
        createNewCategory("childCategory", "parentCategory", "childCategoryDescription");

        //== CREATE_NEW_PRODUCT ==//
        createNewProduct(account, "testProduct", "childCategory");
    }

    private void createNewProduct(Account account, String productName, String category) {
        ProductForm productForm = ProductForm.builder()
                .productName(productName)
                .productPrice(50000)
                .productImage("testProductImage")
                .productDescription("testProductDescription")
                .productStock(50)
                .categoryName(category)
                .build();

        Product product = productService.createProduct(account, productForm);
        product.setPublish(true);
    }

    private Account createNewAccount(String nickname, String email, String password) {
        SignUpForm signUpForm = SignUpForm.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .address(new Address("city", "street", "zipcode"))
                .userOrSeller("user")
                .build();

        Account savedAccount = accountService.createAccount(signUpForm);
        changeToAdmin(nickname);
        return savedAccount;
    }

    private void changeToAdmin(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        account.setRole(Role.ROLE_ADMIN);
        account.setMemberGrade(memberGradeRepository.findByGradeName("FAMILY"));
    }

    private void createNewCategory(String title, String parentCategoryTitle, String description) {
        CategoryCreateForm form = CategoryCreateForm.builder()
                .title(title)
                .parentCategoryTitle(parentCategoryTitle)
                .description(description)
                .build();

        Category category = categoryService.createNewCategory(form);
        category.setPublish(true);
    }

    @AfterEach
    void afterEach() {
        productRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
    }


    @DisplayName("카테고리 별 상품 조회 - 입력 값 정상")
    @Test
    void view_all_published_product_by_category_with_correct_value() throws Exception {

        mockMvc.perform(get("/product?category={categoryTitle}", "childCategory")
                .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("productList", "categoryTitle"))
                .andExpect(view().name("product/list"))
                .andExpect(unauthenticated());
    }

    @DisplayName("카테고리 별 상품 조회 - 입력 값 오류")
    @Test
    void view_all_published_product_by_category_with_wrong_value() throws Exception {

        assertThatThrownBy(() -> {
            mockMvc.perform(get("/product?category={categoryTitle}", "not_categorized"))
                    .andExpect(status().is5xxServerError());
        }).hasCause(new IllegalArgumentException("존재하지 않는 카테고리입니다."));
    }


    @DisplayName("상품 상세 조회 - 입력 값 정상")
    @Test
    void view_detail_product_info_with_correct_value() throws Exception {

        Product testProduct = productRepository.findByProductName("testProduct");

        mockMvc.perform(
                get("/product/info?id={id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("product"))
                .andExpect(view().name("product/detail"));
    }

    @DisplayName("상품 상세 조회 - 입력 깂 오류")
    @Test
    void view_detail_product_info_with_wrong_value() throws Exception {

        assertThatThrownBy(() -> {
            mockMvc.perform(get("/product/info?id={id}", "999999"))
                    .andExpect(status().is5xxServerError());
        }).hasCause(new IllegalArgumentException("존재하지 않는 상품입니다."));
    }


}