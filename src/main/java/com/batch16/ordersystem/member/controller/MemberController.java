package com.batch16.ordersystem.member.controller;

import com.batch16.ordersystem.common.auth.JwtTokenProvider;
import com.batch16.ordersystem.common.dto.CommonDto;
import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.member.dto.*;
import com.batch16.ordersystem.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<?> createMember(@RequestBody MemberCreateDto memberCreateDto) {
        Long id = memberService.save(memberCreateDto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("회원가입이 완료되었습니다.")
                        .build(),
                HttpStatus.CREATED
        );
    }

    // rt를 통한 at 토큰 재발급 (갱신)
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody RefreshTokenDto refreshTokenDto) {
        // rt 검증 로직
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());
        // db의 기존 rt와 비교하여 일치하는지 확인
        String accessToken = jwtTokenProvider.createAtToken(member);
        // at 신규 생성 로직
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .build();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberLoginResDto)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("새로운 access token 발급 성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        List<MemberListDto> memberList = memberService.findAll();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberList)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("회원 목록 조회 성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/myInfo")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> myInfo() {
        return new ResponseEntity<>(
                CommonDto
                        .builder()
                        .result(memberService.myInfo())
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("내 정보 조회 성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@Valid @RequestBody MemberLoginReqDto memberLoginReqDto) {
        Member member = memberService.doLogin(memberLoginReqDto);
        String accessToken = jwtTokenProvider.createAtToken(member);
        String refreshToken = jwtTokenProvider.createRtToken(member);
        MemberLoginResDto loginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginResDto)
                        .statusCode(HttpStatus.OK.value())
                        .statusMessage("로그인 성공")
                        .build(),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id) {
        memberService.delete(id);
        return "OK";
    }
}
