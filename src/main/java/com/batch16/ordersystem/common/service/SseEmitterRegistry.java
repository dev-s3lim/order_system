package com.batch16.ordersystem.common.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
    // SseEmitter는 연결된 사용자 정보를 의미한다. (ip, mac 주소 등)
    // concurrentHashMap은 thread-safe한 맵으로, 멀티스레드 환경에서 안전하게 사용할 수 있다 (동시성 발생 없음)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void addSseEmitter(String email, SseEmitter sseEmitter) {
        emitterMap.put(email, sseEmitter);
    }

    public void removeSseEmitter(String email) {
        emitterMap.remove(email);
    }

    public SseEmitter getEmitter(String email) {
        return emitterMap.get(email);
    }
}
