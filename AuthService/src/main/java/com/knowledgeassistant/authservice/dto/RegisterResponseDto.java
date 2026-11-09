package com.knowledgeassistant.authservice.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.knowledgeassistant.authservice.enums.AccountStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponseDto {
    private long id;
    
    private String username;
    
    private String email;
    
    private AccountStatus accountStatus;
    
    private Set<String> roles;
    
    private LocalDateTime createdAt ;
}
