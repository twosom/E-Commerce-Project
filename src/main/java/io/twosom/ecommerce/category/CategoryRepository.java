package io.twosom.ecommerce.category;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByTitle(String title);

    @EntityGraph(value = "Category.withChildCategory")
    List<Category> findAllByParentCategoryIsNullAndPublish(boolean publish);


    List<Category> findAllByParentCategoryIsNull();

    @EntityGraph(attributePaths = {"parentCategory", "childCategory"})
    List<Category> findAllByParentCategoryIsNotNull();

    @EntityGraph(attributePaths = {"parentCategory", "childCategory"})
    List<Category> findAll();

    boolean existsByTitle(String title);
}
