package com.project.hugme.domain.user.dto;

import com.project.hugme.domain.user.entity.User;

public record MyInfoResponse(String name,String email) {

    public static MyInfoResponse from(User user) {
        return new MyInfoResponse(
                user.getName(),
                user.getEmail()
        );
    }
}
