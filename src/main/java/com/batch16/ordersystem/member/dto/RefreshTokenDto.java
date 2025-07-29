package com.batch16.ordersystem.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RefreshTokenDto {
    private String refreshToken;
    // parameter로 rt를 받는 경우는 보안에 취약할 수 있음
    // url에 parameter 값이 노출되기 때문
    // 따라서 중요한 값은 body로 받는 것이 좋음
}
