package com.project.hugme.domain.user.controller;


import com.project.hugme.domain.user.dto.MyInfoResponse;
import com.project.hugme.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "회원 API",
        description = "회원 정보 조회 및 회원 탈퇴 API"
)
public class UserController {


    private final UserService userService;

    @Operation(
            summary = "내 정보 조회",
            description = "로그인한 사용자의 회원 정보를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getMyInfo(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        MyInfoResponse response = userService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 회원 상태를 탈퇴 상태로 변경하고 Refresh Token을 폐기합니다."
    )
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }
}
