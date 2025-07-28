package com.batch16.ordersystem.member.dto;

import com.batch16.ordersystem.member.domain.Member;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberCreateDto {
    @NotEmpty(message = "고객님의 성함을 입력해 주세요")
    private String name;
    @NotEmpty(message = "고객님의 이메일을 입력해 주세요")
    private String email;
    @NotEmpty(message = "고객님의 비밀번호를 입력해 주세요")
    @Size(min = 8, message = "비밀번호가 너무 짧습니다. 8자 이상 입력해 주세요.")
    private String password;
    @Builder.Default
    private String role = "USER";

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
                .role(this.role)
                .build();
    }
}
