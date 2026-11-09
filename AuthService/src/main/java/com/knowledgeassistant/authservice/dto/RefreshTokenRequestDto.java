package com.knowledgeassistant.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {

	@NotBlank(message = "refresh token can not be blan")
	private String refreshToken;
}
