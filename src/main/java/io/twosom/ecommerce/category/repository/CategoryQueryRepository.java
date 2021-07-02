package io.twosom.ecommerce.category.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.twosom.ecommerce.category.CategoryDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.*;

import static com.querydsl.core.group.GroupBy.*;
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

    public Map<String, List<String>> getCategoryTitleList() {

        return queryFactory.from(category)
                .where(category.publish.isTrue())
                .transform(groupBy(category.parentCategory.title).as(list(category.title)));

    }

    public Map<List<?>, List<String>> getCategoryTitleForNav() {

        return queryFactory.from(category)
                .where(category.publish.isTrue())
                .transform(groupBy(category.parentCategory.title, category.parentCategory.description)
                        .as(list(category.title)));
    }
}
