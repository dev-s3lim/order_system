package com.batch16.ordersystem.member.dto;

import com.batch16.ordersystem.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MemberListDto {
    private String name;
    private String email;

    public static MemberListDto fromEntity(Member member) {
        return new MemberListDto(
                member.getName(),
                member.getEmail()
        );
    }
}
