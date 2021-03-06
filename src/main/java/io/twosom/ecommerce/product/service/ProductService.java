package io.twosom.ecommerce.product.service;

import io.twosom.ecommerce.account.domain.Account;
import io.twosom.ecommerce.category.Category;
import io.twosom.ecommerce.category.repository.CategoryRepository;
import io.twosom.ecommerce.product.domain.Product;
import io.twosom.ecommerce.product.domain.SaleHistory;
import io.twosom.ecommerce.product.form.ProductForm;
import io.twosom.ecommerce.product.repository.ProductRepository;
import io.twosom.ecommerce.product.repository.SaleHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SaleHistoryRepository saleHistoryRepository;

    private final ModelMapper modelMapper;

    public Product createProduct(Account account, ProductForm productForm) {
        Product product = modelMapper.map(productForm, Product.class);
        product.setSeller(account);
        Category category = categoryRepository.findByTitle(productForm.getCategoryName());
        product.setCategory(category);

        return productRepository.save(product);
    }

    public void updateProduct(Long productId, ProductForm productForm) {
        Product findProduct = productRepository.findById(productId).get();
        modelMapper.map(productForm, findProduct);
        setSaleIfActivated(productForm, findProduct);
        setCategoryIfChanged(productForm, findProduct);
    }

    public void removeProduct(Long productId, Account account) {
        Product product = findProductAndValidateSeller(productId, account);

        productRepository.delete(product);
    }

    public void publishProduct(Long productId, Account account) {
        Product product = findProductAndValidateSeller(productId, account);
        product.setPublish(true);
    }

    private Product findProductAndValidateSeller(Long productId, Account account) {
        Product product = productRepository.findById(productId).get();
        if (!product.getSeller().equals(account)) {
            throw new IllegalStateException(account.getNickname() + "??? ?????? ????????? ???????????? ????????????.");
        }
        return product;
    }

    public void unPublishProduct(Long productId, Account account) {
        Product product = findProductAndValidateSeller(productId, account);
        product.setPublish(false);
    }

    private void setSaleIfActivated(ProductForm productForm, Product findProduct) {
        if (productForm.getSaleRate() > 0) {
            findProduct.setSale(true);
            findProduct.setSalePrice(findProduct.calculateSalePrice());
        } else if (productForm.getSaleRate() == 0) {
            findProduct.setSale(false);
            findProduct.setSalePrice(0);
        }
        saveSaleHistory(findProduct);
    }

    private void saveSaleHistory(Product findProduct) {
        SaleHistory saleHistory = new SaleHistory();
        saleHistory.setProduct(findProduct);
        saleHistory.setOriginalPrice(findProduct.getProductPrice());
        saleHistory.setSalePrice(findProduct.calculateSalePrice());
        saleHistory.setSaleRate(findProduct.getSaleRate());
        saleHistoryRepository.save(saleHistory);
    }

    private void setCategoryIfChanged(ProductForm productForm, Product findProduct) {
        Category category = categoryRepository.findByTitle(productForm.getCategoryName());
        if (!findProduct.getCategory().equals(category)) {
            findProduct.setCategory(category);
        }
    }
}
