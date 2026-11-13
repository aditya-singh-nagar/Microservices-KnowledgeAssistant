package com.knowledgeassistant.authservice.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgeassistant.authservice.customexception.ResourceNotFoundException;
import com.knowledgeassistant.authservice.dto.InternalUserDto;
import com.knowledgeassistant.authservice.dto.UpdateStatusRequestDto;
import com.knowledgeassistant.authservice.entities.Role;
import com.knowledgeassistant.authservice.entities.User;
import com.knowledgeassistant.authservice.enums.AccountStatus;
import com.knowledgeassistant.authservice.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
public class InternalUserController {

	private final UserRepository userRepo;
	
	@GetMapping("/{id}")
	public ResponseEntity<InternalUserDto> getUserById(@PathVariable @Valid Long id){
		User user = userRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("User with the id does not exist"));
		
		return ResponseEntity.ok(mapToDto(user));
	}
	
	@PutMapping("/{id}/status")
	public ResponseEntity<InternalUserDto> updateUserStatus(@PathVariable Long id, @RequestBody UpdateStatusRequestDto dto){
		User user = userRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("User with the id does not exist"));
		user.setAccountStatus(dto.getAccountStatus());
		user.setApprovedBy(dto.getApprovedBy());
		if(dto.getAccountStatus()==AccountStatus.ACTIVE) {
			user.setApprovedAt(LocalDate.now());
		}
		User saved = userRepo.save(user);
		
		return ResponseEntity.ok(mapToDto(saved));
	}
	
    @GetMapping
    public ResponseEntity<List<InternalUserDto>> getAllUsers() {
        List<InternalUserDto> users = userRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
    @GetMapping("/pending")
    public ResponseEntity<List<InternalUserDto>> getPendingUsers() {
        List<InternalUserDto> users = userRepo.findByAccountStatus(AccountStatus.PENDING_APPROVAL).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
	
    private InternalUserDto mapToDto(User user) {
        return InternalUserDto.builder()
                .id(user.getId())
                .username(user.getUserName())
                .email(user.getEmail())
                .accountStatus(user.getAccountStatus())
                .approvedBy(user.getApprovedBy())
                .roles(user.getRoles().stream()
                        .map(Role::getRole)
                        .collect(Collectors.toSet()))
                .build();
    }
	
}
