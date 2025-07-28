package com.batch16.ordersystem.member.service;

import com.batch16.ordersystem.member.domain.Member;
import com.batch16.ordersystem.member.dto.MemberCreateDto;
import com.batch16.ordersystem.member.dto.MemberDetailDto;
import com.batch16.ordersystem.member.dto.MemberListDto;
import com.batch16.ordersystem.member.dto.MemberLoginReqDto;
import com.batch16.ordersystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long save(MemberCreateDto memberCreateDto) {
        memberRepository.findByEmail(memberCreateDto.getEmail())
                .ifPresent(member -> {
                    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
                });
        String encodedPassword = passwordEncoder.encode(memberCreateDto.getPassword());
        Member member = memberRepository.save(memberCreateDto.toEntity(encodedPassword));
        return member.getId();
    }


    public Member doLogin(MemberLoginReqDto loginReqDto) {
        Optional<Member> optionalAuthor = memberRepository.findByEmail(loginReqDto.getEmail());
        boolean check = true;
        if (optionalAuthor.isPresent()){
            check = false;
        }
        if (check) {
            throw new NoSuchElementException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        return optionalAuthor.get();
    }

    @Transactional(readOnly = true)
    public List<MemberListDto> findAll() {
        return memberRepository.findAll().stream().map(m -> MemberListDto.fromEntity(m)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberDetailDto findById(Long id) throws NoSuchElementException {
        Member member = memberRepository.findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));
        MemberDetailDto memberDetailDto = MemberDetailDto.fromEntity(member);
        return memberDetailDto;
    }

    @Transactional(readOnly = true)
    public MemberDetailDto myInfo(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));
        return MemberDetailDto.fromEntity(member);
    }

    @Transactional
    public void delete(Long id) throws NoSuchElementException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        if (!member.getId().equals(id)) {
            throw new NoSuchElementException("해당 회원을 삭제할 권한이 없습니다.");
        }

        member.delete("Y");
    }
}
