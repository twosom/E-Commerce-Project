package io.twosom.ecommerce.account.form;

import io.twosom.ecommerce.account.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpForm {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{3,20}$")
    @Length(min = 3, max = 20)
    private String nickname;

    @Size(min = 8,max = 50)
    private String password;

    private Address address;

}
