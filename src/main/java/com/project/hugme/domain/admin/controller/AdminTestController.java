package com.project.hugme.domain.admin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(
        name = "관리자 API",
        description = "관리자 권한 API"
)
public class AdminTestController {

    @GetMapping("/test")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("관리자 접근 성공");
    }
}