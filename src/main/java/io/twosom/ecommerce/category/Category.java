package io.twosom.ecommerce.category;

import io.twosom.ecommerce.category.form.CategoryEditForm;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NamedEntityGraph(name = "Category.withChildCategory",
attributeNodes = {@NamedAttributeNode(value = "childCategory")})
@Entity
@Builder
@Getter @Setter @EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
@ToString(exclude = "childCategory")
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String title;

    private String description;

    @Column(nullable = false)
    private boolean publish = false;

    @Lob
    private String image;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<Category> childCategory = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    public void addChildCategory(Category category) {
        getChildCategory().add(category);
        category.setParentCategory(this);
    }

    public void removeChildCategory(Category category) {
        getChildCategory().remove(category);
        category.setParentCategory(null);
    }

    public void updateTitleAndDescription(CategoryEditForm categoryEditForm) {
        this.title = categoryEditForm.getTitle();
        this.description = categoryEditForm.getDescription();
    }
}
