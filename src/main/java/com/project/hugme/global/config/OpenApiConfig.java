package com.project.hugme.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HugMe API",
                description = "HugMe API 명세서",
                version = "v1"
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer applicationApiOrderCustomizer() {

        return openApi -> {

            Paths originalPaths =
                    openApi.getPaths();

            if (originalPaths == null) {
                return;
            }

            /*
             * 원하는 Swagger 표시 순서
             */
            List<String> apiPathOrder =
                    List.of(
                            /*
                             * 인증 API
                             */
                            "/api/auth/signup",
                            "/api/auth/mail/check",
                            "/api/auth/mail/verify",
                            "/api/auth/login",
                            "/api/auth/token/reissue",
                            "/api/auth/logout",

                            /*
                             * 사용자 맞춤 체크리스트 API
                             */
                            "/api/applications",
                            "/api/applications/current",
                            "/api/applications/{applicationId}/lease-contract",
                            "/api/applications/{applicationId}/info",
                            "/api/applications/{applicationId}/questions",
                            "/api/applications/{applicationId}/answers",
                            "/api/applications/{applicationId}/result-documents",
                            "/api/applications/{applicationId}/documents"
                    );
            Paths reorderedPaths =
                    new Paths();

            /*
             * 지정한 신청 API를 원하는 순서대로 먼저 넣는다.
             */
            for (String path : apiPathOrder) {

                if (originalPaths.containsKey(path)) {
                    reorderedPaths.addPathItem(
                            path,
                            originalPaths.get(path)
                    );
                }
            }

            /*
             * 인증 등 나머지 API는 기존 순서대로 넣는다.
             */
            originalPaths.forEach(
                    (path, pathItem) -> {

                        if (!reorderedPaths.containsKey(path)) {
                            reorderedPaths.addPathItem(
                                    path,
                                    pathItem
                            );
                        }
                    }
            );

            openApi.setPaths(reorderedPaths);
        };
    }
}