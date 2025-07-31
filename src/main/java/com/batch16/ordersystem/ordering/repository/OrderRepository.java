package com.batch16.ordersystem.ordering.repository;

import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository <Ordering, Long> {
    // 회원의 주문 목록 조회
    List<Ordering> findAllByMember(Member member);
}
