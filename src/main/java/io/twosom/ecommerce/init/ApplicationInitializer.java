package io.twosom.ecommerce.init;

import io.twosom.ecommerce.account.domain.MemberGrade;
import io.twosom.ecommerce.account.repository.MemberGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationInitializer implements ApplicationRunner {

    private final MemberGradeRepository memberGradeRepository;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        createIfNotExistsMemberGrade("FAMILY", 1);
        createIfNotExistsMemberGrade("SILVER", 5);
        createIfNotExistsMemberGrade("GOLD", 7);
        createIfNotExistsMemberGrade("VIP", 10);
    }

    private void createIfNotExistsMemberGrade(String gradeName, int pointSaveRate) {
        MemberGrade familyGrade = memberGradeRepository.findByGradeName(gradeName);
        if (familyGrade == null) {
            familyGrade = MemberGrade.builder()
                    .gradeName(gradeName)
                    .pointSaveRate(pointSaveRate)
                    .build();
        }

        memberGradeRepository.save(familyGrade);
    }
}
