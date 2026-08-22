package com.project.hugme.domain.auth.security;

import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.entity.UserRole;
import com.project.hugme.domain.user.entity.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID=1L;

    private final Long userId;
    private final String email;
    private final String password;
    private final UserRole role;
    private final UserStatus status;

    public static CustomUserDetails from(User user){
        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getStatus()
        );
    }
//역할을 받으면 ROLE_붙여서 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(
                new SimpleGrantedAuthority("ROLE_"+role.name())
        );

    }
//로그인하면 무엇으로 식별하나
    @Override
    public String getUsername(){
        return email;
    }
//사용자 상태가 로그인 가능한지 확인
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
/*
계정만료 체크
isAccountNonExpired()

계정 잠김 확인
예를 들어 로그인 5회 실패 시 계정을 잠그는 기능을 만들 때 사용
isAccountNonLocked()

비밀번호 만료 확인
예를 들어 90일마다 비밀번호를 변경하도록 하는 정책이 있을 때 사용
isCredentialsNonExpired()

 */

}
