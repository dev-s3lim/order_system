package com.batch16.ordersystem.product.domain;

import com.batch16.ordersystem.common.domain.BaseTimeEntity;
import com.batch16.ordersystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
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

    private String productImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 등록자 정보

    public void updateImageUrl(String imgUrl) {
        this.productImage = imgUrl;
    }
}