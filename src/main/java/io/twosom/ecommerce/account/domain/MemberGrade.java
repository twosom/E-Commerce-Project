package io.twosom.ecommerce.account.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class MemberGrade {

    @Id @GeneratedValue
    private Long id;

    private String gradeName;

    private int pointSaveRate;
}
