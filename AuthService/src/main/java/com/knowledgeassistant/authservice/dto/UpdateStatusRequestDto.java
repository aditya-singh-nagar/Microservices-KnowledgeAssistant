package com.knowledgeassistant.authservice.dto;

import com.knowledgeassistant.authservice.enums.AccountStatus;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UpdateStatusRequestDto {

	AccountStatus accountStatus;
	
	String approvedBy;
}
