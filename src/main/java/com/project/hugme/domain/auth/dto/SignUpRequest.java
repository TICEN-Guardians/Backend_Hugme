package com.project.hugme.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest( @NotBlank(message="이메일은 필수입니다.")
                             @Email(message="이메일 형식이 올바르지 않습니다.")
                             String email,

                             @NotBlank(message="비밀번호는 필수입니다.")
                             @Size(min=4,max=100,message="비밀번호는 4~100자 입니다.")
                             String password,

                             @NotBlank(message = "이름은 필수입니다.")
                             @Size(max=10,message="이름은 10자 이하여야 합니다.")
                             String name) {

}
