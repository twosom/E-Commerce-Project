package io.twosom.ecommerce.shoppingbag;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.shoppingbag.domain.ShoppingBag;
import io.twosom.ecommerce.shoppingbag.exception.NotValidShoppingBagException;
import io.twosom.ecommerce.shoppingbag.exception.ProductStockNotEnoughException;
import io.twosom.ecommerce.shoppingbag.form.ShoppingBagForm;
import io.twosom.ecommerce.shoppingbag.repository.ShoppingBagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingBagService {

    private final ShoppingBagRepository shoppingBagRepository;
    private final ProductRepository productRepository;

    public Long add(Account account, ShoppingBagForm shoppingBagForm) {
        Product findProduct = findProductAndValidateIsNull(shoppingBagForm);

        if (findProduct.getProductStock() < shoppingBagForm.getQuantity()) {
            throw new ProductStockNotEnoughException("재고가 부족합니다.");
        }

        ShoppingBag shoppingBag = createNewShoppingBag(account, shoppingBagForm, findProduct);
        minusProductStock(shoppingBagForm, findProduct);
        return shoppingBagRepository.save(shoppingBag).getId();
    }

    private ShoppingBag createNewShoppingBag(Account account, ShoppingBagForm shoppingBagForm, Product findProduct) {
        ShoppingBag shoppingBag = new ShoppingBag();
        if (findProduct.isSale()) {
            shoppingBag.setSalePrice(findProduct.calculateSalePrice());
        }
        shoppingBag.setAccount(account);
        shoppingBag.setProduct(findProduct);
        shoppingBag.setQuantity(shoppingBagForm.getQuantity());
        shoppingBag.setStatus(ShoppingBagStatus.STANDBY);
        return shoppingBag;
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


    public void validateAccount(List<ShoppingBag> shoppingBagProductList, Account account) {
        for (ShoppingBag shoppingBag : shoppingBagProductList) {
            if (!shoppingBag.getAccount().equals(account)) {
                throw new NotValidShoppingBagException("잘못된 접근입니다.");
            }
        }
    }
}
