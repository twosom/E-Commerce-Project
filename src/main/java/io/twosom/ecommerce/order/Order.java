package io.twosom.ecommerce.order;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.Address;
import io.twosom.ecommerce.shoppingbag.ShoppingBagStatus;
import io.twosom.ecommerce.shoppingbag.domain.ShoppingBag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.utility.RandomString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
public class Order {

    @Id
    private String id;

    @ManyToOne
    Account account;

    @CreatedDate
    private LocalDateTime orderedDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<ShoppingBag> shoppingBagList = new ArrayList<>();

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private Payment payment;

    private int totalSumPrice;

    public static Order createNewOrder(Account account, Address address, List<ShoppingBag> shoppingBagList, int totalSumPrice, Payment payment) {
        Order order = new Order();

        String id = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + RandomString.make(8);
        order.setId(id);
        order.setStatus(OrderStatus.READY);
        order.setAccount(account);
        order.setAddress(address);
        order.setShoppingBagList(shoppingBagList);
        shoppingBagList.forEach(shoppingBag -> {
            shoppingBag.setOrder(order);
            shoppingBag.setStatus(ShoppingBagStatus.ORDERED);
        });
        order.setTotalSumPrice(totalSumPrice);
        order.setPayment(payment);
        return order;
    }
}
