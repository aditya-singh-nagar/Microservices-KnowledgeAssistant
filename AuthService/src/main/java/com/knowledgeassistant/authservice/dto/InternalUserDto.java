package com.knowledgeassistant.authservice.dto;

import java.util.Set;

import com.knowledgeassistant.authservice.enums.AccountStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserDto {
    private Long id;
    private String username;
    private String email;
    private AccountStatus accountStatus;
    private String approvedBy;
    private Set<String> roles;
}
