package io.twosom.ecommerce.account.repository;

import io.twosom.ecommerce.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Account findByEmail(String email);

    long countByEmailVerified(boolean emailVerified);

    Account findByNickname(String nickname);

}
