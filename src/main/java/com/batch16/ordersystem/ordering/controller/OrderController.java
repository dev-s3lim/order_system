package com.batch16.ordersystem.ordering.controller;

import com.batch16.ordersystem.common.dto.CommonDto;
import com.batch16.ordersystem.ordering.domain.Ordering;
import com.batch16.ordersystem.ordering.dto.OrderCreateDto;
import com.batch16.ordersystem.ordering.dto.OrderListResDto;
import com.batch16.ordersystem.ordering.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/ordering")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createConcurrent(@RequestBody List<OrderCreateDto> dto) {
        Long orderId = orderService.createConcurrent(dto);
        return ResponseEntity.ok(CommonDto.builder()
                .result(orderId)
                .statusCode(HttpStatus.CREATED.value())
                .statusMessage("주문 생성 완료")
                .build());
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        List<OrderListResDto> orderListResDtos = orderService.findAll();
        return ResponseEntity.ok(CommonDto.builder()
                .result(orderListResDtos)
                .statusCode(HttpStatus.OK.value())
                .statusMessage("주문 내역 조회 완료")
                .build());
    }

    @GetMapping("/myOrders")
    public ResponseEntity<?> myOrders() {
        List<OrderListResDto> orderListResDtos = orderService.myOrders();
        return ResponseEntity.ok(CommonDto.builder()
                .result(orderListResDtos)
                .statusCode(HttpStatus.OK.value())
                .statusMessage("내 주문 내역 조회 완료")
                .build());
    }
}
