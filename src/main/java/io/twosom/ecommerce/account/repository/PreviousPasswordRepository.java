package io.twosom.ecommerce.account.repository;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.PreviousPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface PreviousPasswordRepository extends JpaRepository<PreviousPassword, Long> {

    List<PreviousPassword> findAllByAccount(Account account);

}
