package com.knowledgeassistant.authservice.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.knowledgeassistant.authservice.customexception.BadRequestException;
import com.knowledgeassistant.authservice.customexception.ForbiddenException;
import com.knowledgeassistant.authservice.customexception.ResourceNotFoundException;
import com.knowledgeassistant.authservice.customexception.UnauthorizedException;
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

import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@Transactional(dontRollbackOn = UnauthorizedException.class)
@AllArgsConstructor
public class AuthService {

	private final RefreshTokenRepository refRepo;
	private final UserRepository userRepo;
	private final RoleRepository roleRepo;
	private final RefreshTokenService refreshTokenService;
	private final TokenBlackListService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtility jwtUtil;
    
    public RegisterResponseDto register(RegisterRequestDto dto) {
    	if (userRepo.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("User already exists with email : " + dto.getEmail());
        }
    	if(userRepo.existsByUserName(dto.getUsername())) {
    		throw new BadRequestException("User already exists with username : " + dto.getUsername());
    	}
    	
    	String rawRole = dto.getRole().trim().toUpperCase();
    	final String requestedRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
    	
    	if(requestedRole.equals("ROLE_ADMIN")) {
    		throw new ForbiddenException("Users cannot register themselves as ADMIN");
    	}
    	Role role = roleRepo.findByRole(requestedRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found in database: " + requestedRole));
    	
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
                 .createdAt(saved.getCreatedAt())
                 .build();
    }
    
    public AuthResponseDto login(LoginDto dto) {
    	User user = userRepo.findByEmail(dto.getEmail())
    			.orElseThrow(()-> new UnauthorizedException("User not found with email: " + dto.getEmail()));
    	if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
    		throw new UnauthorizedException("Invalid password");
    	}
    	if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("Account is not active. Current status: " + user.getAccountStatus());
        }
    	List<String> roles = user.getRoles().stream().map(Role::getRole).collect(Collectors.toList());
    	String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUserName(), user.getEmail(), roles);
    	RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user.getEmail());
    	
    	return AuthResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken.getToken()).build();
    }
    
    public AuthResponseDto refreshAccessToken(RefreshTokenRequestDto dto) {
        RefreshToken refreshToken = refRepo.findByToken(dto.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        User user = refreshToken.getUser();
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("Account is not active. Current status: " + user.getAccountStatus());
        }
        List<String> roles = user.getRoles().stream().map(Role::getRole).collect(Collectors.toList());
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUserName(), user.getEmail(), roles);
        return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public void logout(String accessToken, RefreshTokenRequestDto dto) {
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                Date expiration = jwtUtil.extractExpiration(accessToken);
                tokenBlacklistService.blackListedToken(accessToken, expiration);
            } catch (JwtException | IllegalArgumentException ignored) {
                // Token already expired or malformed; still revoke refresh token
            }
        }
        if (dto != null && dto.getRefreshToken() != null) {
            refreshTokenService.revokeToken(dto.getRefreshToken());
        }
    }

    public void logout(String accessToken, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with the ID does not exist"));

        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthorizedException("Authorization access token is required for logout");
        }

        String emailFromToken = jwtUtil.extractEmail(accessToken);
        if (emailFromToken == null || !emailFromToken.equals(user.getEmail())) {
            throw new ForbiddenException("You can only logout yourself!");
        }

        Date expiration = jwtUtil.extractExpiration(accessToken);
        tokenBlacklistService.blackListedToken(accessToken, expiration);
        refreshTokenService.revokeToken(user);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistService.isBlackListed(token);
    }
	
}
