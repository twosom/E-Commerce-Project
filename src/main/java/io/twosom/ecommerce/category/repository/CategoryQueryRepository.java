package io.twosom.ecommerce.category.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.twosom.ecommerce.category.CategoryDto;
import io.twosom.ecommerce.category.QCategory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static io.twosom.ecommerce.category.QCategory.*;

@Repository
public class CategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CategoryQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<CategoryDto> findAllByParentCategoryIsNotNullToDto() {
        return queryFactory
                .select(Projections.fields(CategoryDto.class,
                        category.id,
                        category.title,
                        category.description,
                        category.publish))
                .from(category)
                .where(category.parentCategory.isNotNull())
                .fetch();
    }

    public List<CategoryDto> findAllByIdInToDto(List<Long> categoryIds) {
        return queryFactory
                .select(Projections.fields(CategoryDto.class,
                        category.id,
                        category.title,
                        category.description,
                        category.publish))
                .from(category)
                .where(category.id.in(categoryIds))
                .fetch();
    }
}
