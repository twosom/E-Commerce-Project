package io.twosom.ecommerce.account.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountPasswordEditForm {

    /* 비밀번호 */
    @Size(min = 8,max = 50)
    private String currentPassword;
    @Size(min = 8,max = 50)
    private String newPassword;
    @Size(min = 8,max = 50)
    private String confirmNewPassword;
}
