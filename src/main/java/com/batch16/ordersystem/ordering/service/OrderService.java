package com.batch16.ordersystem.ordering.service;

import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.member.repository.MemberRepository;
import com.batch16.ordersystem.ordering.domain.OrderDetail;
import com.batch16.ordersystem.ordering.domain.Ordering;
import com.batch16.ordersystem.ordering.dto.OrderCreateDto;
import com.batch16.ordersystem.ordering.dto.OrderListResDto;
import com.batch16.ordersystem.ordering.repository.OrderRepository;
import com.batch16.ordersystem.product.domain.Product;
import com.batch16.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Long create(List<OrderCreateDto> orderCreateDtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보가 없습니다."));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderCreateDto dto : orderCreateDtoList) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품 정보가 없습니다."));
            // 예외를 강제로 발생시키면서, 모든 임시저장 사항들을 전체 롤백 처리한다.
            if( product.getStockQuantity() < dto.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // 1. 동시에 접근하는 상황에서 업데이트 값에 정합성이 깨지고, 갱신이상이 발생할 수 있다.
            // 2. Spring ver이나 MySQL ver에 따라 JPA에서 강제 에러 (deadlock)를 유발시켜 대부분의 요청 실패 발생
            // DB와 JPA 격리수준이 다를 경우 보통 DB의 격리 수준을 따름
            // 갱신이상은 update 문제가 아닌 select 문제로 발생하는 경우가 많음
            // ex : 조회 시점부터 데이터의 무결성을 위해 락을 걸어야하는데, 이렇게 되면 성능 저하가 발생한다.
            product.updateStockQuantity(dto.getProductCount()); // 재고 수량 업데이트 (순서는 어차피 다 롤백되기 때문에 상관없음)

            OrderDetail detail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .member(member)
                    .build();
            ordering.getOrderDetailList().add(detail); // 자식까지 저장되도록 설정
        }
        orderRepository.save(ordering); // 주문 저장
        return ordering.getId();
    }

    public List<OrderListResDto> findAll(){
        return orderRepository.findAll().stream().map(o->OrderListResDto.fromEntity(o)).collect(Collectors.toList());
    }


    public List<OrderListResDto> myOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("member is not found"));
        return  orderRepository.findAllByMember(member).stream().map(o->OrderListResDto.fromEntity(o)).collect(Collectors.toList());
    }
}
