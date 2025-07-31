package com.batch16.ordersystem.common.service;

import com.batch16.ordersystem.common.constant.Role;
import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
//CommandLineRunner 인터페이스를 구현함으로서 run 메서드가 애플리케이션 시작 시 자동 실행됨
@Component
public class InitialDataLoader implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (memberRepository.findByEmail("admin@naver.com").isPresent()){
            return;
        }
        Member member = Member.builder()
                .name("관리자")
                .email("admin@naver.com")
                .role(Role.ADMIN)
                //.role("ADMIN")
                .password(passwordEncoder.encode("12345678"))
                .build();
        memberRepository.save(member);
    }
}