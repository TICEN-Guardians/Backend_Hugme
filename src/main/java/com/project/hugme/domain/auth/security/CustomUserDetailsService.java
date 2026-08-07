package com.project.hugme.domain.auth.security;

import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.entity.UserStatus;
import com.project.hugme.domain.user.exception.AlreadyWithdrawnUserException;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->
                        new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return CustomUserDetails.from(user);
    }

    public CustomUserDetails loadUserById(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->
                        new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return CustomUserDetails.from(user);
    }


}
