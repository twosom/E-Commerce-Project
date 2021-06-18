package io.twosom.ecommerce.shoppingbag;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.twosom.ecommerce.product.form.ProductForm;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.product.service.ProductService;
import io.twosom.ecommerce.shoppingbag.form.ShoppingBagForm;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ShoppingBagControllerTest {

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
    ShoppingBagRepository shoppingBagRepository;

    @Autowired
    ShoppingBagService shoppingBagService;


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
        shoppingBagRepository.deleteAll();
        productRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("장바구니에 추가 - 성공")
    @Test
    void add_shopping_bag_list_success() throws Exception {

        Account account = accountRepository.findByNickname("twosom");
        Product findProduct = productRepository.findByProductName("testProduct");

        ShoppingBagForm form = ShoppingBagForm.builder()
                .productId(findProduct.getId())
                .quantity(1)
                .build();


        mockMvc.perform(post("/shopping-bag/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(form))
                .with(csrf()))
                .andExpect(status().isOk());

        List<ShoppingBag> shoppingBagList = shoppingBagRepository.findByAccountAndStatus(account, ShoppingBagStatus.STANDBY);
        assertNotNull(shoppingBagList);
        assertEquals(shoppingBagList.size(), 1);

    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("장바구니에 추가 - 실패")
    @Test
    void add_shopping_bag_list_failed() throws Exception {

        Product findProduct = productRepository.findByProductName("testProduct");

        ShoppingBagForm form = ShoppingBagForm.builder()
                .productId(findProduct.getId())
                .quantity(51)
                .build();


        mockMvc.perform(post("/shopping-bag/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(form))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("장바구니에서 삭제 - 성공")
    @Test
    void minus_shopping_bag_list_success() throws Exception {

        add_shopping_bag_list_success();

        Account account = accountRepository.findByNickname("twosom");
        Product findProduct = productRepository.findByProductName("testProduct");

        ShoppingBagForm form = ShoppingBagForm.builder()
                .productId(findProduct.getId())
                .build();


        mockMvc.perform(post("/shopping-bag/minus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(form))
                .with(csrf()))
                .andExpect(status().isOk());


        List<ShoppingBag> shoppingBagCancelList = shoppingBagRepository.findByAccountAndStatus(account, ShoppingBagStatus.CANCEL);
        assertNotNull(shoppingBagCancelList);
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("장바구니에서 삭제 - 실패")
    @Test
    void minus_shopping_bag_list_failed() throws Exception {

        add_shopping_bag_list_success();

        ShoppingBagForm form = ShoppingBagForm.builder()
                .productId(99999L)
                .build();


        mockMvc.perform(post("/shopping-bag/minus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(form))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

}