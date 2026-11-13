package com.knowledgeassistant.usermanagementservice.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.knowledgeassistant.usermanagementservice.dto.UpdateStatusRequestDto;
import com.knowledgeassistant.usermanagementservice.dto.UserResponseDto;

@FeignClient(name = "AUTHSERVICE",path = "/api/internal/users")
public interface AuthServiceClient {

	@GetMapping("/{id}")
    UserResponseDto getUserById(@PathVariable("id") Long id);
	
    @GetMapping
    List<UserResponseDto> getAllUsers();
    
    @GetMapping("/pending")
    List<UserResponseDto> getPendingUsers();
    
    @PutMapping("/{id}/status")
    UserResponseDto updateUserStatus(
            @PathVariable("id") Long id,
            @RequestBody UpdateStatusRequestDto dto);	
}
