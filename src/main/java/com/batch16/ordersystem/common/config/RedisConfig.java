package com.batch16.ordersystem.common.config;

import com.batch16.ordersystem.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// 검색의 경우 elastic search 등 사용 가능
// 이벤트 상품 재고처리, 좋아요 등 -> 동시성 처리 문제로 인해 Redis 사용
// 1. 갱신이상
// 2. 동시성 문제
// 3. 분산락
// 4. 캐시
// 5. 세션 관리
// 6. 멀티/싱글 스레드
// 7. 인메모리의 키밸류 기반 (성능 빠름)
// 8. Deadlock 문제

// redis는 영속적인 데이터를 저장하지 않기 때문에 일정한 시간마다 rdb에도 업데이트를 쳐줘야 하는데, 이를 위해 메시지 (Json형태) 큐잉 서비스 (RabbitMQ 등)를 도입했다.
// 메시지를 매번 치면 동시성 이슈가 또 발생하기 때문에, 일단 큐에 메시지를 발행하고, 이를 비동기적으로 처리하는 방식으로 구현한다.

// ex : 100명의 사용자가 동시에 들어왔다고 했을때, redis에는 잘 저장되지만, rdb에도 업데이트를 쳐줘야하지만, 멀티스레드 형태이기 때문에 또 동시성 이슈가 발생한다.
// 이를 해결하기 위해 RabbitMQ (싱글스레드)를 사용하여 메시지를 발행하고, 이를 비동기적으로 처리하는 방식으로 구현한다.

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    @Bean
    // Qualifier: 같은 Bean 객체가 여러개 있을 경우 Bean 객체를 구분하기 위한 어노테이션
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("stockInventory")
    public RedisConnectionFactory stockConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("rtInventory")
    // Bean들끼리 서로 의존성을 주입받을 때 메서드 파라미터로도 주입받을 수 있다.
    // 모든 template 중 redisTemplate 이란 이름이 반드시 1개는 있어야 함
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("stockInventory")
    public RedisTemplate<String, String> stockTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // redis pub/sub을 위한 연결 객체 생성
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory sseFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        //redis pub/sub 기능은 db의 값을 저장하는 기능이 아니므로, 특정 기능에 의존적이지 않음 (db configuration 필요 없음)
        return new LettuceConnectionFactory(configuration);
        //kafka는 안정적이다, redis는 빠르다.
    }

    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> ssePubSubTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // redis pub/sub을 위한 메시지 리스너 객체
    // ************ 만약에 알림 주제가 1개 이상이면 여기서 PatternTopic을 여러개 추가해주면 된다. ************
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter
    ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
        return container;
        // 만약에 여러 채널을 구독해야하는 경우, 여러개의 PatternTopic을 add하거나, 별도의 Bean 객체 생성하여 주입해주면 된다.
    }

    // redis 채널에서 수신된 메시지를 처리하는 빈 객체
    @Bean
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {
        // 채널로부터 수신되는 message 자리를 SseAlarmService의 onMessage 메서드로 설정
        // 즉, 메시지가 수신되면 onMessage 메서드가 호출된다.
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }
}
