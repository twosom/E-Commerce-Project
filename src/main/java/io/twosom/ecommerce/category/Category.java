package io.twosom.ecommerce.category;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String title;

    private String description;

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
}
