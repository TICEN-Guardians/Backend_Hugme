package com.project.hugme.domain.auth.dto;

public record TokenReissueResponse(
        String accessToken,

        String tokenType


) {

    public static TokenReissueResponse of(
            String accessToken
    ){
        return new TokenReissueResponse(
                accessToken,"Bearer"
        );
    }
}
