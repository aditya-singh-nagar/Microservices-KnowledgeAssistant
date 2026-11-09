package com.knowledgeassistant.usermanagementservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequestDto {
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email cannot be blank")
    private String email;
}