package com.batch16.ordersystem.common.service;

import com.batch16.ordersystem.common.dto.StockRabbitMqDto;
import com.batch16.ordersystem.product.domain.Product;
import com.batch16.ordersystem.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockRabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;

    // rabbitMq에 메시지 발생
    public void publish (Long productId, int productCount) {
        StockRabbitMqDto dto = StockRabbitMqDto.builder()
                .productId(productId)
                .productCount(productCount)
                .build();
        rabbitTemplate.convertAndSend("stockDecreaseQueue", dto);
    }

    // rabbitMq에 발행된 메시지 수신 (여기서는 단일 스레드로 작동하기 때문에 동시성 이슈 발생 안함)
    // Listener는 단일 스레드로 메시지를 처리하므로, 동시성 이슈 발생없음

    /*
    @Transactional
    @RabbitListener(queues = "stockDecreaseQueue")
    public void subscribe(Message message) throws JsonProcessingException {
        String messageBody = new String (message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        StockRabbitMqDto dto = objectMapper.readValue(messageBody, StockRabbitMqDto.class);
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException());
        product.updateStockQuantity(dto.getProductCount());
        System.out.println(messageBody);
    }
     */

    @Transactional
    @RabbitListener(queues = "stockDecreaseQueue")
    public void subscribe(Message message) {
        String messageBody = new String(message.getBody());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StockRabbitMqDto dto = objectMapper.readValue(messageBody, StockRabbitMqDto.class);

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> {
                        log.error("상품 ID [{}] 존재하지 않음", dto.getProductId());
                        return new EntityNotFoundException("상품이 존재하지 않습니다.");
                    });

            product.updateStockQuantity(dto.getProductCount());
            log.info("재고 감소 완료: {}", messageBody);

        } catch (Exception e) {
            log.error("재고 감소 메시지 처리 실패: {}, 에러: {}", messageBody, e.getMessage(), e);
            throw new RuntimeException("RabbitMQ 메시지 처리 중 오류", e);
        }
    }
}
