package io.twosom.ecommerce.order.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.order.dto.OrderDto;
import io.twosom.ecommerce.order.dto.ProductDtoForOrderDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;

import static com.querydsl.core.group.GroupBy.list;
import static io.twosom.ecommerce.account.domain.QAccount.*;
import static io.twosom.ecommerce.order.QOrder.*;
import static io.twosom.ecommerce.product.domain.QProduct.*;
import static io.twosom.ecommerce.shoppingbag.domain.QShoppingBag.*;

@Repository
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    public OrderQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    public List<OrderDto> findAllByAccount(Account account) {
        List<OrderDto> orderDtoList = getOrderDto(account);
        List<String> orderIds = extractOrderIdList(orderDtoList);
        List<ProductDtoForOrderDto> productDtoList = getProductDtoListByOrderIdIn(orderIds);
        Map<String, List<ProductDtoForOrderDto>> stringListMap = divideByOrderId(productDtoList);
        stringListMap.forEach((orderId, productDtoForOrderDtoList) -> orderDtoList.forEach(o -> {
            if (o.getOrderId().equals(orderId)) {
                o.setProductDtoList(productDtoForOrderDtoList);
            }
        }));

        return orderDtoList;
    }

    private List<String> extractOrderIdList(List<OrderDto> orderDtoList) {
        List<String> orderIds = orderDtoList.stream().map(OrderDto::getOrderId).collect(Collectors.toList());
        return orderIds;
    }

    private Map<String, List<ProductDtoForOrderDto>> divideByOrderId(List<ProductDtoForOrderDto> productDtoList) {
        return productDtoList.stream().collect(Collectors.groupingBy(ProductDtoForOrderDto::getOrderId));
    }

    private List<ProductDtoForOrderDto> getProductDtoListByOrderIdIn(List<String> orderIds) {
        return queryFactory.select(Projections.fields(ProductDtoForOrderDto.class,
                shoppingBag.product.id,
                shoppingBag.product.productName,
                shoppingBag.product.productImage,
                shoppingBag.product.seller.nickname.as("sellerName"),
                shoppingBag.order.id.as("orderId"),
                shoppingBag.quantity,
                new CaseBuilder().when(shoppingBag.salePrice.gt(0)).then(shoppingBag.salePrice)
                        .otherwise(shoppingBag.product.productPrice).as("productPrice")
                )).from(shoppingBag)
                .where(shoppingBag.order.id.in(orderIds))
                .fetch();
    }

    private List<OrderDto> getOrderDto(Account account) {
        return queryFactory
                .select(Projections.fields(OrderDto.class,
                        order.id.as("orderId"),
                        order.orderedDate,
                        order.totalSumPrice,
                        order.savedPoint,
                        order.address,
                        order.payment,
                        order.status))
                .from(order)
                .where(order.account.eq(account))
                .orderBy(order.orderedDate.desc())
                .fetch();
    }

    public OrderDto getOrderDtoForModalById(String orderId) {
        //TODO 메소드 명 정하기
        OrderDto orderDto = findOrderDtoById(orderId);
        List<ProductDtoForOrderDto> productDtoList = getProductDtoListById(orderId);
        orderDto.setProductDtoList(productDtoList);

        return orderDto;
    }

    private List<ProductDtoForOrderDto> getProductDtoListById(String orderId) {
        return queryFactory.select(Projections.fields(ProductDtoForOrderDto.class,
                shoppingBag.product.id,
                shoppingBag.product.productName,
                shoppingBag.product.seller.nickname.as("sellerName"),
                shoppingBag.quantity,
                shoppingBag.order.id.as("orderId"),
                shoppingBag.product.productImage,
                new CaseBuilder().when(shoppingBag.salePrice.gt(0)).then(shoppingBag.salePrice)
                        .otherwise(shoppingBag.product.productPrice).as("productPrice")
        )).from(shoppingBag)
                .where(shoppingBag.order.id.eq(orderId))
                .fetch();
    }

    private OrderDto findOrderDtoById(String orderId) {
        return queryFactory
                .select(Projections.fields(OrderDto.class,
                        order.id.as("orderId"),
                        order.orderedDate,
                        order.totalSumPrice,
                        order.address,
                        order.payment,
                        order.status))
                .from(order)
                .where(order.id.eq(orderId))
                .fetchOne();
    }

    public Map<Account, List<String>> findSellerAndProductFromOrderById(String orderId) {

        return queryFactory
                .from(shoppingBag)
                .leftJoin(shoppingBag.product, product)
                .leftJoin(shoppingBag.product.seller, account)
                .where(shoppingBag.order.id.eq(orderId))
                .transform(groupBy(shoppingBag.product.seller).as(list(shoppingBag.product.productName)));
    }
}
