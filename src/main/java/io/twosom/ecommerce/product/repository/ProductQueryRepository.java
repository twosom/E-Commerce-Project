package io.twosom.ecommerce.product.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.dto.ProductViewDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<String> getAllProductNamesForSearch() {
        return queryFactory
                .select(product.productName)
                .from(product)
                .where(product.publish.isTrue())
                .fetch();
    }

    public List<String> getAllProductNamesByKeyword(String keyword) {
        List<Tuple> fetch = queryFactory
                .select(product.productName, product.sellCount)
                .from(product)
                .where(product.productName.like("%" + keyword + "%").and(product.publish.isTrue()))
                .fetch();
        return fetch.stream().map(e -> {
            return e.get(product.productName) + " - 판매량 : " + e.get(product.sellCount);
        }).collect(Collectors.toList());
    }

    public Long getProductIdByProductName(String productName) {
        return queryFactory
                .select(product.id)
                .from(product)
                .where(product.productName.eq(productName))
                .fetchOne();
    }

    public List<Product> getAllProductByKeyword(String keyword) {
        return queryFactory
                .select(product)
                .from(product)
                .where(product.productName.like("%" + keyword + "%")
                        .and(product.publish.isTrue()))
                .fetch();
    }
}
