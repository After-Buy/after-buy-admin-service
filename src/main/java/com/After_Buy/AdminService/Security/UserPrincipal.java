package com.After_Buy.AdminService.Security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 정보 코어 주체 객체
 *
 * @author 최준혁
 */
@Getter
@RequiredArgsConstructor
public class UserPrincipal {
    private final Long userId;
}
