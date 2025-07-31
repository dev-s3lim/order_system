package com.batch16.ordersystem.ordering.domain;

import com.batch16.ordersystem.common.domain.BaseTimeEntity;
import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OrderDetail -> Product 관계 = N:1 (한 개의 주문 상세가 하나의 상품에 속한다.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    // OrderDetail -> Ordering 관계 = N:1 (한 개의 주문 상세가 하나의 주문에 속한다.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;

    // OrderDetail -> Member 관계 = N:1 (한 개의 주문 상세가 하나의 회원에 속한다.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
