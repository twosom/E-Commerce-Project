package io.twosom.ecommerce.order.repository;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findAllByAccount(Account account);

}
