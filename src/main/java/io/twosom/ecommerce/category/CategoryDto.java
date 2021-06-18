package io.twosom.ecommerce.category;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {

    private Long id;
    private String title;
    private String description;
    private boolean publish;
    private List<CategoryDto> childCategory = new ArrayList<>();
//    private CategoryDto parentCategory;
}
