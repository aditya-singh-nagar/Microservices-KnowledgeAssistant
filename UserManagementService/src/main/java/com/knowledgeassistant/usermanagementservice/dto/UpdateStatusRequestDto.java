package com.knowledgeassistant.usermanagementservice.dto;

import com.knowledgeassistant.usermanagementservice.enums.AccountStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDto {
    private AccountStatus accountStatus;
    private String approvedBy;
}