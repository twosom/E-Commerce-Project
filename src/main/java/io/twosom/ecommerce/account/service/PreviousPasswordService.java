package io.twosom.ecommerce.account.service;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.account.domain.PreviousPassword;
import io.twosom.ecommerce.account.repository.PreviousPasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PreviousPasswordService {

    private final PreviousPasswordRepository previousPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean isDuplicatePassword(Account account, String newPassword) {
        List<PreviousPassword> previousPasswordList = previousPasswordRepository.findAllByAccount(account);

        return validateDuplicatePassword(newPassword, previousPasswordList);

    }

    private boolean validateDuplicatePassword(String newPassword, List<PreviousPassword> previousPasswordList) {
        List<PreviousPassword> collect = previousPasswordList.stream().filter(previousPassword -> {
            if (previousPassword.getPasswordChangedDate().isBefore(LocalDateTime.now().minusMonths(3))) {
                return false;
            }

            if (!passwordEncoder.matches(newPassword, previousPassword.getEncodedPreviousPassword())) {
                return false;
            }
            return true;

//            if (!previousPassword.getPasswordChangedDate().isBefore(LocalDateTime.now().minusMonths(3))
//                    || passwordEncoder.matches(newPassword, previousPassword.getEncodedPreviousPassword())) {
//
//                return true;
//            }
//            return false;
        }).collect(Collectors.toList());

        return collect.size() > 0;
    }
}
