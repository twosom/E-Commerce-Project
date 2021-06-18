package io.twosom.ecommerce.product.repository;

import io.twosom.ecommerce.product.domain.SaleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface SaleHistoryRepository extends JpaRepository<SaleHistory, Long> {
}
