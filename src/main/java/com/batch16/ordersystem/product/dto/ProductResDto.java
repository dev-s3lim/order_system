package com.batch16.ordersystem.product.dto;

import com.batch16.ordersystem.product.domain.Product;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductResDto {
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private Long memberId; // 등록자 ID

    public static ProductResDto fromEntity(Product product) {
        return ProductResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .memberId(product.getMember() != null ? product.getMember().getId() : null) // 등록자 ID
                .build();
    }
}
