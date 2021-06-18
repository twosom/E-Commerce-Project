package io.twosom.ecommerce.shoppingbag.repository;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.shoppingbag.ShoppingBag;
import io.twosom.ecommerce.shoppingbag.ShoppingBagStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ShoppingBagRepository extends JpaRepository<ShoppingBag, Long> {

    boolean existsByAccountAndProductAndStatus(Account account, Product product, ShoppingBagStatus status);

    ShoppingBag findByAccountAndProductAndStatus(Account account, Product product, ShoppingBagStatus status);

    long countByAccountAndStatus(Account account, ShoppingBagStatus status);

    List<ShoppingBag> findByAccountAndStatus(Account account, ShoppingBagStatus status);
}
