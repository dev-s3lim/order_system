package com.batch16.ordersystem.ordering.domain;

import com.batch16.ordersystem.common.constant.OrderStatus;
import com.batch16.ordersystem.common.domain.BaseTimeEntity;
import com.batch16.ordersystem.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Ordering extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    // Ordering -> Member 관계 = N:1 (한 명의 회원이 여러 주문을 할 수 있다.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // Ordering -> OrderDetail 관계 = 1:N (Order Detail이 N개 생성될 수 있다.)
    // CascadeType 사용 목적 -> 자식 엔티티인 OrderDetail이 Ordering 엔티티에 종속되어 있기 때문에, Ordering을 저장할 때 OrderDetail도 함께 저장되도록 설정
    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST) // 자식 엔티티까지 삭제하려면 CascadeType.ALL로 변경
    @Builder.Default
    private List<OrderDetail> orderDetailList = new ArrayList<>();
}

