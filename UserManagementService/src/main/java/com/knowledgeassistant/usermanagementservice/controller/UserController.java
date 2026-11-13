package com.knowledgeassistant.usermanagementservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgeassistant.usermanagementservice.customexception.ForbiddenException;
import com.knowledgeassistant.usermanagementservice.dto.UserResponseDto;
import com.knowledgeassistant.usermanagementservice.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. Get logged-in user's own profile
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile(
            @RequestHeader("X-User-Id") Long callerId) {
        return ResponseEntity.ok(userService.getUser(callerId));
    }

    // 2. Get user by ID (Admin/Manager can view anyone; normal users can only view themselves)
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long callerId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String callerRole) {

        boolean isPrivileged = "ROLE_ADMIN".equals(callerRole) || "ROLE_MANAGER".equals(callerRole);
        if (!isPrivileged && !callerId.equals(id)) {
            throw new ForbiddenException("Access denied: You can only view your own profile.");
        }

        return ResponseEntity.ok(userService.getUser(id));
    }

    // 3. Get all users (Admin / Manager only)
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String callerRole) {

        if (!"ROLE_ADMIN".equals(callerRole) && !"ROLE_MANAGER".equals(callerRole)) {
            throw new ForbiddenException("Access denied: Only ADMIN or MANAGER can view all users.");
        }

        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 4. Get pending approval users (Scoped by caller's role in UserService)
    @GetMapping("/pending")
    public ResponseEntity<List<UserResponseDto>> getPendingUsers(
            @RequestHeader("X-User-Id") Long callerId) {

 //       String callerEmail = getCallerEmail(callerId);
        return ResponseEntity.ok(userService.getPendingUser(callerId));
    }

    // 5. Approve a pending user (Hierarchical checks enforced in UserService)
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long approverId) {

 //       String approverEmail = getCallerEmail(approverId);
        String message = userService.approveUser(id, approverId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // 6. Reject a pending user (Hierarchical checks enforced in UserService)
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long approverId) {

  //      String approverEmail = getCallerEmail(approverId);
        String message = userService.rejectUser(id, approverId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // Helper: Resolve caller's email securely from the Gateway-verified X-User-Id
//    private String getCallerEmail(Long callerId) {
//        return userService.getUser(callerId).getEmail();
//    }
}