package com.project.hugme.domain.auth.dto;

public record LoginResponse(
        Long UserId,
        String email,
        String name

) {
}
