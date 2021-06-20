package io.twosom.ecommerce.shoppingbag.repository;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.shoppingbag.domain.ShoppingBag;
import io.twosom.ecommerce.shoppingbag.ShoppingBagStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.List;

@Transactional(readOnly = true)
public interface ShoppingBagRepository extends JpaRepository<ShoppingBag, Long> {

    boolean existsByAccountAndProductIdAndStatus(Account account, Long productId, ShoppingBagStatus status);

    ShoppingBag findByAccountAndProductAndStatus(Account account, Product product, ShoppingBagStatus status);

    long countByAccountAndStatus(Account account, ShoppingBagStatus status);

    List<ShoppingBag> findByAccountAndStatus(Account account, ShoppingBagStatus status);

    @EntityGraph(attributePaths = {"account", "product"})
    List<ShoppingBag> findAllByIdIn(List<Long> ids);

    boolean existsByProductPublishAndId(boolean publish, Long id);
}
