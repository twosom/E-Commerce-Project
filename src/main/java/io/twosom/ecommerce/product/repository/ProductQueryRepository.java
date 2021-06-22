package io.twosom.ecommerce.product.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.dto.ProductDto;
import io.twosom.ecommerce.product.dto.ProductDtoForAdminAndSeller;
import io.twosom.ecommerce.product.dto.ProductIndexViewDto;
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


    public List<ProductDto> findAllByCategoryTitleAndPublished(String categoryTitle) {
        return queryFactory.select(Projections.fields(ProductDto.class,
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

    public List<ProductDto> getAllProductByKeyword(String keyword) {
        return queryFactory
                .select(Projections.fields(ProductDto.class,
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
                .where(product.productName.like("%" + keyword + "%")
                        .and(product.publish.isTrue()))
                .fetch();
    }

    public List<ProductDtoForAdminAndSeller> findAllToProductDtoForAdminMenu() {
        return queryFactory
                .select(Projections.fields(ProductDtoForAdminAndSeller.class,
                        product.id,
                        product.productName,
                        product.productPrice,
                        product.productStock,
                        product.sellCount,
                        product.seller.nickname.as("sellerName"),
                        product.publish,
                        product.sale,
                        product.category.id.as("categoryId")))
                .from(product)
                .fetch();
    }

    public List<ProductDtoForAdminAndSeller> findAllBySellerToDto(Account account) {
        return queryFactory
                .select(Projections.fields(ProductDtoForAdminAndSeller.class,
                        product.id,
                        product.productName,
                        product.seller.nickname.as("sellerName"),
                        product.productPrice,
                        product.productStock,
                        product.sellCount,
                        product.publish,
                        product.sale,
                        product.category.id.as("categoryId")))
                .from(product)
                .where(product.seller.eq(account))
                .fetch();
    }

    public List<ProductIndexViewDto> getTop10OrderBySellCountDescToDto() {
        return queryFactory
                .select(Projections.fields(ProductIndexViewDto.class,
                        product.id,
                        product.productName,
                        product.productImage,
                        product.category.title.as("categoryTitle"),
                        product.productPrice,
                        product.sale,
                        product.saleRate,
                        product.seller.nickname.as("sellerName")))
                .from(product)
                .where(product.publish.isTrue())
                .orderBy(product.sellCount.desc())
                .limit(10)
                .fetch();

    }
}
