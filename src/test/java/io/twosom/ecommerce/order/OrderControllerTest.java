package io.twosom.ecommerce.order;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.account.domain.MemberGrade;
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
import io.twosom.ecommerce.order.repository.OrderRepository;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.form.ProductForm;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.product.service.ProductService;
import io.twosom.ecommerce.shoppingbag.ShoppingBagService;
import io.twosom.ecommerce.shoppingbag.domain.ShoppingBag;
import io.twosom.ecommerce.shoppingbag.form.ShoppingBagForm;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import org.apache.tomcat.util.buf.StringUtils;
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

import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

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

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    EntityManager em;

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
        Long productAId = createNewProduct(account, "testAProduct", "childCategory", 50);
        Long productBId = createNewProduct(account, "testBProduct", "childCategory", 50);

        //== CREATE_NEW_SHOPPING_BAG ==//
        createNewShoppingBag(account, productAId);
        createNewShoppingBag(account, productBId);
    }

    private void createNewShoppingBag(Account account, Long productId) {
        ShoppingBagForm shoppingBagForm = ShoppingBagForm.builder()
                .productId(productId)
                .quantity(1)
                .build();

        shoppingBagService.add(account, shoppingBagForm);
    }

    private Long createNewProduct(Account account, String productName, String category, int productStock) {
        ProductForm productForm = ProductForm.builder()
                .productName(productName)
                .productPrice(50000)
                .productImage("testProductImage")
                .productDescription("testProductDescription")
                .productStock(productStock)
                .categoryName(category)
                .build();

        Product product = productService.createProduct(account, productForm);
        product.setPublish(true);
        return product.getId();
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
        MemberGrade memberGrade = memberGradeRepository.findByGradeName("FAMILY");
        System.out.println("memberGrade = " + memberGrade);
        account.setMemberGrade(memberGrade);
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
        orderRepository.deleteAll();
        productRepository.deleteAll();
        accountRepository.deleteAll();
        categoryRepository.deleteAll();
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("장바구니를 통한 구매 - 폼 접근")
    @Test
    void order_with_shopping_bag_form() throws Exception {
        String idArray = getShoppingBagIdArrayToString();

        mockMvc.perform(get("/order/form")
                .param("idArray", idArray))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("address", "shoppingBagList", "orderForm", "totalSumPrice"))
                .andExpect(view().name("order/order-form"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("장바구니를 통한 구매 - 성공")
    @Test
    void order_with_shopping_bag_with_correct_value() throws Exception {
        String idArray = getShoppingBagIdArrayToString();

        mockMvc.perform(
                post("/order")
                        .param("idArray", idArray)
                        .param("payment", Payment.CREDIT_CARD.name())
                        .param("city", "CITY")
                        .param("street", "STREET")
                        .param("zipcode", "ZIPCODE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list"))
                .andExpect(flash().attributeExists("orderDtoForModal"));

        List<Order> orders = orderRepository.findAll();
        assertEquals(orders.size(), 1);
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("장바구니를 통한 구매 - 실패")
    @Test
    void order_with_shopping_bag_with_wrong_value() throws Exception {

        String idArray = getShoppingBagIdArrayToString();
        mockMvc.perform(
                post("/order")
                        .param("idArray", idArray)
                        .param("payment", Payment.CREDIT_CARD.name())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name("order/order-form"));
    }


    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("주문 목록 모두 조회")
    @Test
    void order_list() throws Exception {
        mockMvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/list"))
                .andExpect(model().attributeExists("orderReadyList", "orderCompList", "orderCancelList"));
    }

    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("장바구니를 통해서가 아닌 직접 구매")
    @Test
    void order_without_shopping_bag_add() throws Exception {

        List<Long> idList = productRepository.findAll().stream().map(Product::getId).collect(Collectors.toList());
        Long productId = idList.get(0);


        mockMvc.perform(post("/order/direct")
                .param("productId", productId.toString())
                .param("quantity", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
    
    @WithUserDetails(value = "twosom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("구매 확정")
    @Test
    void order_confirm() throws Exception {
        order_with_shopping_bag_with_correct_value();
        List<Order> orders = orderRepository.findAll();
        Order order = orders.get(0);
        String orderId = order.getId();

        mockMvc.perform(
                post("/order/confirmation")
                        .param("orderId", orderId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/list"));

        assertTrue(order.getSavedPoint() > 0);
    }


    private String getShoppingBagIdArrayToString() {
        List<String> collect = shoppingBagRepository.findAll().stream()
                .map(ShoppingBag::getId)
                .map(Object::toString)
                .collect(Collectors.toList());

        return StringUtils.join(collect, ',');
    }

}