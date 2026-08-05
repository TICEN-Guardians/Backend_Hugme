package com.project.hugme.domain.auth.dto;

import com.project.hugme.domain.user.entity.User;

public record SignUpResponse(
        Long userId,
        String email,
        String name


) {
    public static SignUpResponse from(User user){
        return new SignUpResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName()
        );
    }
}
