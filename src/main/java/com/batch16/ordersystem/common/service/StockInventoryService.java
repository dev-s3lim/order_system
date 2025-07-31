package com.batch16.ordersystem.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockInventoryService {

    private final RedisTemplate<String, String> redisTemplate;

    public StockInventoryService(@Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 상품 등록 시 재고 수량 세팅
    public void makeStockQuantity(Long productId, Integer quantity) {
        redisTemplate.opsForValue().set(String.valueOf(productId), String.valueOf(quantity));
    }

    // 주문 성공 시 재고 수량 감소
    public int decreaseStockQuantity(Long productId, int orderQuantity) {
        String remainObject = redisTemplate.opsForValue().get(String.valueOf(productId));
        int remains = Integer.parseInt(remainObject);
        if (remains < orderQuantity) {
            return -1;
        } else {
            Long finalRemains = redisTemplate.opsForValue().decrement(String.valueOf(productId), orderQuantity);
            return finalRemains.intValue();
        }
    }

    // 주문 취소 시 재고 수량 증가
    public void increaseStockQuantity(Long productId, Integer quantity) {
        redisTemplate.opsForValue().increment(String.valueOf(productId), quantity);
    }
}

