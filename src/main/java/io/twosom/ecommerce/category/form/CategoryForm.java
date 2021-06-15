package io.twosom.ecommerce.category.form;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryForm {

    private String parentCategoryTitle;
    private String title;
    private String description;
}
