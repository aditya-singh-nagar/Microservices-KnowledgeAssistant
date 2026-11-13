package com.knowledgeassistant.authservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.knowledgeassistant.authservice.customexception.ResourceNotFoundException;
import com.knowledgeassistant.authservice.customexception.UnauthorizedException;
import com.knowledgeassistant.authservice.entities.RefreshToken;
import com.knowledgeassistant.authservice.entities.User;
import com.knowledgeassistant.authservice.repository.RefreshTokenRepository;
import com.knowledgeassistant.authservice.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(dontRollbackOn = UnauthorizedException.class)
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refRepo;
	
	private final UserRepository userRepo;
	
	@Value("${jwt.refresh-token.expiration:604800000}")
	private long refreshTokenValidity;
	
	public RefreshToken generateRefreshToken(String email) {
		User user = userRepo.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
		
		RefreshToken refreshToken = refRepo.findByUser(user)
                .orElse(new RefreshToken());
		
		String token = UUID.randomUUID().toString();
		
		refreshToken.setUser(user);
		refreshToken.setToken(token);
		refreshToken.setRevoked(false);
		refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenValidity));
		return refRepo.save(refreshToken);
		
	}
	
	public RefreshToken verifyExpiration(RefreshToken token) {
		if(token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
			refRepo.delete(token);
			throw new UnauthorizedException("Refresh token was expired or revoked. Please login again.");
		}
		return token;
	}
	
	public void revokeToken(String token) {
		RefreshToken rToken = refRepo.findByToken(token)
				.orElseThrow(()-> new ResourceNotFoundException("Refresh token not found"));
		rToken.setRevoked(true);
		refRepo.save(rToken);
	}
	
	public void revokeToken(User user) {
		refRepo.findByUser(user)
				.ifPresent(r -> {
					r.setRevoked(true);
					refRepo.save(r);
				});
	}
}
