package com.knowledgeassistant.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    @NotBlank
    @Size(min = 4, max = 50 , message = "username must be greater then 4 and less then 50 ")
    private String username;

    @NotBlank
    @Size(min = 8, max = 50 , message = "password must be greater then 8 and less then 50 ")
    @Pattern(
    	    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    	    message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    	)
    private String password;

    @NotBlank
    @Email(message = "Invalid email format")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@algoworks\\.com$",
        message = "Email must belong to the @algoworks.com domain"
    )
    private String email;

    @NotBlank(message = "Requested role cannot be blank")
    private String role;
}
