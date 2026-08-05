package com.project.hugme.domain.user.controller;


import com.project.hugme.domain.user.dto.MyInfoResponse;
import com.project.hugme.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getMyInfo(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        MyInfoResponse response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }
}
