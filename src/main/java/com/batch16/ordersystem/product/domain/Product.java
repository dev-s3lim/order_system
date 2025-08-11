package com.batch16.ordersystem.product.domain;

import com.batch16.ordersystem.common.domain.BaseTimeEntity;
import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Builder
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stockQuantity;

    private String productImage; // 이미지 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void updateProduct(ProductUpdateDto productUpdateDto) {
        this.name = productUpdateDto.getName();
        this.category = productUpdateDto.getCategory();
        this.price = productUpdateDto.getPrice();
        this.stockQuantity = productUpdateDto.getStockQuantity();
    }

    public void updateImageUrl(String imgUrl) {
        this.productImage = imgUrl;
    }

    public String getImagePath() {
        return this.productImage;
    }

    public void updateStockQuantity(Integer orderedQuantity) {
        this.stockQuantity -= orderedQuantity;
    }
}
