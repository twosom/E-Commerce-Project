package io.twosom.ecommerce.product.repository;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.product.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"seller", "category", "category.childCategory"})
    List<Product> findAll();

    Product findByProductName(String productName);

    List<Product> findAllBySeller(Account account);

    List<Product> findByCategoryAndPublish(Category category, boolean publish);

    Product findByIdAndPublish(Long id, boolean publish);

    List<Product> getTop10ByOrderBySellCountDesc();
}
