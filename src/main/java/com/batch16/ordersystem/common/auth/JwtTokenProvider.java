package com.batch16.ordersystem.common.auth;

import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expirationAt}")
    private int expirationAt;
    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;
    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secret_at_key;
    // 실무에서는 rt도 별로로 관리 해주면 더 좋음
    private Key secret_rt_key;

    // Qualifier는 기본적으로 메서드를 통한 주입이 가능. 그래서, 이 경우, 생성자 주입방식을 해야 qualifier를 사용할 수 있음
    public JwtTokenProvider(MemberRepository memberRepository, @Qualifier ("rtInventory")RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        secret_at_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyAt), SignatureAlgorithm.HS512.getJcaName());
        secret_rt_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyRt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createAtToken(Member member) {

        String email = member.getEmail();
        String name = member.getName();
        String role = member.getRole();

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("name", name);
        claims.put("role", role);

        Date now = new Date();

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationAt * 60 * 1000L))
                .signWith(secret_at_key)
                .compact();
        return accessToken;
    }
    // accessToken만 사용시 보안상 약점
    // refreshToken을 사용하여 accessToken을 재발급하는 방식으로 보완 가능
    // refreshToken은 별도의 저장소 (rdb 또는 redis 등)에 저장하여 관리
    // redis는 인메모리 (해시값) 데이터베이스로 빠른 속도와 높은 성능을 제공
    // 싱글스레드 환경에서 동작하여 멀티스레드 환경에서의 동기화 문제를 피할 수 있음
    // accessToken은 자주 갱신되므로 짧은 유효기간을 설정하고, refreshToken은 상대적으로 긴 유효기간을 설정하여 보안성을 높임

    public  Member validateRt(String refreshToken) {
        // rt 그 자체를 검증
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email).orElseThrow(()
                -> new RuntimeException("Member not found"));
        // redis 값과 비교하는 검증
        String redisRt = redisTemplate.opsForValue().get(member.getEmail());
        if(!redisRt.equals(refreshToken)){
            throw new IllegalArgumentException("Invalid refresh token");
        }
        return member;
    }

    public String createRtToken(Member member){
        String email = member.getEmail();
        String name = member.getName();
        String role = member.getRole();

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("name", name);
        claims.put("role", role);

        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationAt * 60 * 1000L))
                .signWith(secret_rt_key)
                .compact();

        // redis 에 rt 저장
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);
        // redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS); // 여기서도 만료 시간 설정 가능
        return refreshToken;
    }
}
