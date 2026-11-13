package com.knowledgeassistant.authservice.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgeassistant.authservice.dto.AuthResponseDto;
import com.knowledgeassistant.authservice.dto.LoginDto;
import com.knowledgeassistant.authservice.dto.RefreshTokenRequestDto;
import com.knowledgeassistant.authservice.dto.RegisterRequestDto;
import com.knowledgeassistant.authservice.dto.RegisterResponseDto;
import com.knowledgeassistant.authservice.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        RegisterResponseDto response = authService.register(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
        AuthResponseDto response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto dto) {
        AuthResponseDto response = authService.refreshAccessToken(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody RefreshTokenRequestDto dto) {
        String accessToken = extractBearerToken(authHeader);
        authService.logout(accessToken, dto);
        return ResponseEntity.ok(Map.of("message", "You have been logged out successfully."));
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<Map<String, String>> logoutById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        String accessToken = extractBearerToken(authHeader);
        authService.logout(accessToken, id);
        return ResponseEntity.ok(Map.of("message", "You have been logged out successfully."));
    }

    @PostMapping("/blacklist/check")
    public ResponseEntity<Map<String, Boolean>> isTokenBlacklisted(@RequestBody Map<String, String> payload) {
        String token = payload != null ? payload.get("token") : null;
        return ResponseEntity.ok(Map.of("blacklisted", authService.isTokenBlacklisted(token)));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }
}