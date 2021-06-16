package io.twosom.ecommerce.category.form;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryEditForm {

    private Long id;

    private String parentCategoryTitle;
    @NotBlank
    private String title;

    private String description;
}
