package com.batch16.ordersystem.common.service;

import com.batch16.ordersystem.common.dto.SseMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final RedisTemplate<String, String> redisTemplate;

    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.redisTemplate = redisTemplate;
    }

    // 특정 사용자에게 메시지 발송
    public void publishMessage(String receiver, String sender, Long orderingId) {
        SseMessageDto sseMessageDto = SseMessageDto.builder()
                .receiver(receiver)
                .sender(sender)
                .orderingId(orderingId)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String data = null;
        try {
            data = objectMapper.writeValueAsString(sseMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);
        // emitter 객체가 현재 서버에 있으면, 직접 알림 발송. 그렇지 않으면 redis 에 publish
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event().name("Ordered").data(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            // redis에 publish
            redisTemplate.convertAndSend("order-channel", data);
        }
        /// 사용자가 로그아웃 후에 다시 돌아왔을 때도 알림메시지가 남아있으려면 DB에 추가적으로 저장 필요.
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // message는 실질적인 메시지 내용이 담긴 객체
        // pattern은 채널명
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            String channel_name = new String(pattern); // <- 여러개의 채널을 구독하고 있을 경우, 채널명으로 분기 처리

            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());
            if (sseEmitter != null) {
                try {
                    sseEmitter.send(SseEmitter.event().name("Ordered").data(dto));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
/*
    public void addSseEmitter() {
        SseEmitter sseEmitter = new SseEmitter(14400 * 1000L * 60);   // 10일 정도 emitter 유효기간 성정
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        emitterMap.put(email, sseEmitter);
        System.out.println(emitterMap);
    }
 */

}
