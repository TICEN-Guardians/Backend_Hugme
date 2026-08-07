package com.project.hugme.domain.auth.dto;

public record TokenReissueResponse(
        String accessToken,
        String refreshToken,
        String tokenType


) {

    public static TokenReissueResponse of(
            String accessToken,
            String refreshToken
    ){
        return new TokenReissueResponse(
                accessToken,refreshToken,"Bearer"
        );
    }
}
