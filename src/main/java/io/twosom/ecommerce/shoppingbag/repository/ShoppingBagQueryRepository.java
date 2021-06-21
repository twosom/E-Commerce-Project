package io.twosom.ecommerce.shoppingbag.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.shoppingbag.ShoppingBagStatus;
import io.twosom.ecommerce.shoppingbag.dto.ShoppingBagListDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static io.twosom.ecommerce.shoppingbag.domain.QShoppingBag.shoppingBag;

@Repository
public class ShoppingBagQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public ShoppingBagQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
        this.em = em;
    }


    public List<ShoppingBagListDto> findStandByOrOrderedShoppingBagByAccount(Account account) {

        return queryFactory.select(Projections.fields(ShoppingBagListDto.class,
                            shoppingBag.id.as("shoppingBagId"),
                            shoppingBag.product.id.as("productId"),
                            shoppingBag.product.productName,
                            shoppingBag.product.productImage,
                            shoppingBag.quantity.as("shoppingBagQuantity"),
                            shoppingBag.product.sale,
                            shoppingBag.product.saleRate,
                            shoppingBag.product.productPrice,
                            shoppingBag.status,
                            shoppingBag.salePrice))
                .from(shoppingBag)
                .where(shoppingBag.account.eq(account).and(shoppingBag.status.eq(ShoppingBagStatus.ORDERED).or(shoppingBag.status.eq(ShoppingBagStatus.STANDBY))))
                .fetch();
    }

    public List<ShoppingBagListDto> findAllByIdIn(List<Long> idArray) {
        return queryFactory.select(Projections.fields(ShoppingBagListDto.class,
                shoppingBag.id.as("shoppingBagId"),
                shoppingBag.product.id.as("productId"),
                shoppingBag.product.productName,
                shoppingBag.product.productImage,
                shoppingBag.quantity.as("shoppingBagQuantity"),
                shoppingBag.product.sale,
                shoppingBag.product.saleRate,
                shoppingBag.product.productPrice,
                shoppingBag.product.salePrice))
                .from(shoppingBag)
                .where(shoppingBag.id.in(idArray))
                .fetch();
    }

    public void updateShoppingBagStatusToConfirmByOrderId(String orderId) {
        new JPAUpdateClause(em, shoppingBag)
                .where(shoppingBag.order.id.eq(orderId))
                .set(shoppingBag.status, ShoppingBagStatus.CONFIRMED)
                .execute();
    }

    public long countByAccount(Account account) {
        return queryFactory
                .from(shoppingBag)
                .where(shoppingBag.account.eq(account)
                        .and(shoppingBag.status.eq(ShoppingBagStatus.ORDERED).or(shoppingBag.status.eq(ShoppingBagStatus.STANDBY))))
                .fetchCount();
    }
}
