package io.twosom.ecommerce.shoppingbag;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.shoppingbag.exception.ProductStockNotEnoughException;
import io.twosom.ecommerce.shoppingbag.form.ShoppingBagForm;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingBagService {

    private final ShoppingBagRepository shoppingBagRepository;
    private final ProductRepository productRepository;

    public void add(Account account, ShoppingBagForm shoppingBagForm) {
        Product findProduct = findProductAndValidateIsNull(shoppingBagForm);

        if (findProduct.getProductStock() < shoppingBagForm.getQuantity()) {
            //TODO 예외 타입 정하기
            throw new ProductStockNotEnoughException("재고가 부족합니다.");
        }


        ShoppingBag shoppingBag = new ShoppingBag();
        shoppingBag.setAccount(account);
        shoppingBag.setProduct(findProduct);
        shoppingBag.setQuantity(shoppingBagForm.getQuantity());
        shoppingBag.setStatus(ShoppingBagStatus.STANDBY);

        minusProductStock(shoppingBagForm, findProduct);

        shoppingBagRepository.save(shoppingBag);
    }


    public void minus(Account account, ShoppingBagForm shoppingBagForm) {
        Product product = findProductAndValidateIsNull(shoppingBagForm);

        ShoppingBag findShoppingBag = shoppingBagRepository.findByAccountAndProductAndStatus(account, product, ShoppingBagStatus.STANDBY);
        if (findShoppingBag == null) {
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }

        plusProductStock(findShoppingBag, product);
        findShoppingBag.setStatus(ShoppingBagStatus.CANCEL);
    }



    private Product findProductAndValidateIsNull(ShoppingBagForm shoppingBagForm) {
        Product findProduct = productRepository.findByIdAndPublish(shoppingBagForm.getProductId(), true);
        if (findProduct == null) {
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }
        return findProduct;
    }

    private void minusProductStock(ShoppingBagForm shoppingBagForm, Product findProduct) {
        int leftStock = findProduct.getProductStock() - shoppingBagForm.getQuantity();
        findProduct.setProductStock(leftStock);
    }

    private void plusProductStock(ShoppingBag shoppingBag, Product product) {
        int restoreStock = product.getProductStock() + shoppingBag.getQuantity();
        product.setProductStock(restoreStock);
    }


}
