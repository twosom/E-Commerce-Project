package io.twosom.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductViewDto {

    private Long id;
    private String productName;
    private String productImage;
    private String seller;
    private String categoryTitle;
    private LocalDateTime createdDate;
    private String productDescription;

    private int productPrice;
    private int productStock;
}
