package io.twosom.ecommerce.account.repository;

import io.twosom.ecommerce.account.domain.MemberGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface MemberGradeRepository extends JpaRepository<MemberGrade, Long> {

    MemberGrade findByGradeName(String gradeName);
}
