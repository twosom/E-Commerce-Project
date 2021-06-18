package io.twosom.ecommerce.product.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.twosom.ecommerce.product.dto.ProductViewDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static io.twosom.ecommerce.product.domain.QProduct.product;

@Repository
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ProductQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    public List<ProductViewDto> findAllByCategoryTitleAndPublished(String categoryTitle) {
        return queryFactory.select(Projections.fields(ProductViewDto.class,
                            product.id,
                            product.productName,
                            product.productImage,
                            product.seller.nickname.as("seller"),
                            product.productPrice,
                            product.productStock,
                            product.category.title.as("categoryTitle"),
                            product.createdDate,
                            product.sale,
                            product.saleRate))
                .from(product)
                .where(product.category.title.eq(categoryTitle)
                        .and(product.publish.isTrue()))
                .fetch();
    }

    public ProductViewDto findByIdAndPublished(Long id) {
        return queryFactory.select(Projections.fields(ProductViewDto.class,
                            product.id,
                            product.productName,
                            product.productImage,
                            product.seller.nickname.as("seller"),
                            product.productPrice,
                            product.productStock,
                            product.category.title.as("categoryTitle"),
                            product.createdDate,
                            product.productDescription,
                            product.sale,
                            product.saleRate))
                .from(product)
                .where(product.id.eq(id).and(product.publish.isTrue()))
                .fetchOne();
    }
}
