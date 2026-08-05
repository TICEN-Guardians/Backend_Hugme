package com.project.hugme.domain.auth.service;


import com.project.hugme.domain.auth.dto.LoginRequest;
import com.project.hugme.domain.auth.dto.LoginResponse;
import com.project.hugme.domain.auth.dto.SignUpRequest;
import com.project.hugme.domain.auth.dto.SignUpResponse;
import com.project.hugme.domain.auth.exception.DuplicateEmailException;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request)  {

        if(userRepository.existsByEmail(request.email())){
            throw new DuplicateEmailException();
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createLocalUser(
                request.email(),
                encodedPassword,
                request.name()
        );

        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest request){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )

        );
        User user = userRepository.findByEmail(request.email())
        .orElseThrow(()-> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return new LoginResponse(user.getUserId(),user.getEmail(),user.getName());

    }
}
