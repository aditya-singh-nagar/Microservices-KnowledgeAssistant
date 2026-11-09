package com.knowledgeassistant.usermanagementservice.dto;

import com.knowledgeassistant.usermanagementservice.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequestDto {
    @NotNull(message = "Account status is required")
    private AccountStatus status;
}