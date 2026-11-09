package com.knowledgeassistant.usermanagementservice.dto;

import com.knowledgeassistant.usermanagementservice.enums.AccountStatus;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private AccountStatus accountStatus;
    private String approvedBy;
    private Set<String> roles;
}