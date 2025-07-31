package com.batch16.ordersystem.member.domain;

import com.batch16.ordersystem.common.constant.Role;
import com.batch16.ordersystem.common.domain.BaseTimeEntity;
import com.batch16.ordersystem.member.dto.MemberListDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
//jpql을 제외하고, 모든 조회 쿼리에 where del_yn = 'N' 붙이는 효과
@Where(clause = "del_yn = 'N'")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Builder.Default
    private String delYn = "N";
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    /* @Builder.Default
    private String role = "USER";

     */

    public void delete(String delYn) {
        this.delYn = delYn;
    }
}
