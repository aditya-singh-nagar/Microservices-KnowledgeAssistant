package com.knowledgeassistant.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor

public class AuthResponseDto {

	private String accessToken;
	
	private String refreshToken;
	

	
}
