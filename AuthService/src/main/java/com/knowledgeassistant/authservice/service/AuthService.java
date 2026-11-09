package com.knowledgeassistant.authservice.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.knowledgeassistant.authservice.dto.AuthResponseDto;
import com.knowledgeassistant.authservice.dto.LoginDto;
import com.knowledgeassistant.authservice.dto.RefreshTokenRequestDto;
import com.knowledgeassistant.authservice.dto.RegisterRequestDto;
import com.knowledgeassistant.authservice.dto.RegisterResponseDto;
import com.knowledgeassistant.authservice.entities.RefreshToken;
import com.knowledgeassistant.authservice.entities.Role;
import com.knowledgeassistant.authservice.entities.User;
import com.knowledgeassistant.authservice.enums.AccountStatus;
import com.knowledgeassistant.authservice.repository.RefreshTokenRepository;
import com.knowledgeassistant.authservice.repository.RoleRepository;
import com.knowledgeassistant.authservice.repository.UserRepository;
import com.knowledgeassistant.authservice.utility.JWTUtility;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class AuthService {

	private final RefreshTokenRepository refRepo;
	private final UserRepository userRepo;
	private final RoleRepository roleRepo;
	private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtility jwtUtil;
    
    public RegisterResponseDto register(RegisterRequestDto dto) {
    	if (userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User already exists with email : " + dto.getEmail());
        }
    	if(userRepo.existsByUserName(dto.getUsername())) {
    		throw new IllegalArgumentException("User already exists with username : " + dto.getUsername());
    	}
    	
    	String rawRole = dto.getRole().trim().toUpperCase();
    	final String requestedRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
    	
    	if(requestedRole.equals("ROLE_ADMIN")) {
    		throw new IllegalArgumentException("Users cannot register themselves as ADMIN");
    	}
    	Role role = roleRepo.findByRole(requestedRole)
                .orElseThrow(() -> new IllegalArgumentException("Role not found in database: " + requestedRole));
    	
    	 User user = new User();
         user.setUserName(dto.getUsername());
         user.setEmail(dto.getEmail());
         user.setPassword(passwordEncoder.encode(dto.getPassword()));
         Set<Role> roles = new HashSet<>();
         roles.add(role);
         user.setRoles(roles);
         
         if ("ROLE_PUBLIC".equals(requestedRole)) {
             user.setAccountStatus(AccountStatus.ACTIVE);
         } else {
             user.setAccountStatus(AccountStatus.PENDING_APPROVAL);
         }
         
         User saved = userRepo.save(user);
         return RegisterResponseDto.builder()
                 .id(saved.getId())
                 .username(saved.getUserName())
                 .email(saved.getEmail())
                 .accountStatus(saved.getAccountStatus())
                 .roles(saved.getRoles().stream().map(Role::getRole).collect(Collectors.toSet()))
                 
                 .build();
    }
    
    public AuthResponseDto login(LoginDto dto) {
    	User user = userRepo.findByEmail(dto.getEmail()).orElseThrow(()-> new IllegalArgumentException("User not found with email: " + dto.getEmail()));
    	if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
    		throw new IllegalArgumentException("Invalid password");
    	}
    	if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active. Current status: " + user.getAccountStatus());
        }
    	List<String> roles = user.getRoles().stream().map(m->m.getRole()).collect(Collectors.toList());
    	String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUserName(), user.getEmail(), roles);
    	RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user.getEmail());
    	
//    	String primaryRole = user.getRoles().stream()
//                .findFirst()
//                .map(Role::getRole)
//                .orElse("ROLE_PUBLIC");
    	return AuthResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).build();
    }
	
// public AuthResponseDto refreshAccessToken(RefreshTokenRequestDto req) {
//    	
//    	RefreshToken token = refRepo.findByToken(req.getRefreshToken()).orElseThrow(()-> new RuntimeException("Refresh token does not exist in db"));
//    	
//    	refreshTokenService.verifyExpiration(token);
//    	
//    	User user = token.getUser();
//    	List<String> roles = user.getRoles().stream().map(m->m.getRole()).collect(Collectors.toList());
//    	
//    	//for genereting new refresh toeken
//    	String newAccessToken = jwtUtil.generateAccessToken(user.getUserName(), user.getEmail(), roles);
//    	
//    	return AuthResponseDto.builder().refreshToken(token.getToken()).accessToken(newAccessToken).build();
//    }
    
    public AuthResponseDto refreshAccessToken(RefreshTokenRequestDto dto) {
        RefreshToken refreshToken = refRepo.findByToken(dto.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        User user = refreshToken.getUser();
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active. Current status: " + user.getAccountStatus());
        }
        List<String> roles = user.getRoles().stream().map(role->role.getRole()).collect(Collectors.toList());
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUserName(), user.getEmail(), roles);
//        String primaryRole = user.getRoles().stream()
//                .findFirst()
//                .map(Role::getRole)
//                .orElse("ROLE_PUBLIC");
        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
	
}
