package com.knowledgeassistant.authservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.knowledgeassistant.authservice.entities.RefreshToken;
import com.knowledgeassistant.authservice.entities.User;
import com.knowledgeassistant.authservice.repository.RefreshTokenRepository;
import com.knowledgeassistant.authservice.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refRepo;
	
	private final UserRepository userRepo;
	
	private static final long REFRESH_TOKEN_VALIDITY = 7L*24*60*60*1000;
	
	public RefreshToken generateRefreshToken(String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
		
		RefreshToken refreshToken = refRepo.findByUser(user)
                .orElse(new RefreshToken());
		
		String token = UUID.randomUUID().toString();
		
		refreshToken.setUser(user);
		refreshToken.setToken(token);
		refreshToken.setRevoked(false);
		refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY));
		return refRepo.save(refreshToken);
		
	}
	
	public RefreshToken verifyExpiration(RefreshToken token) {
		if(token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
			refRepo.delete(token);
			throw new IllegalArgumentException("Refresh token was expired or revoked. Please login again.");
		}
		return token;
	}
	
	//Revoke token feature is pending to be added
	
}
