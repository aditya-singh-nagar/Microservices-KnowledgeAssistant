package com.knowledgeassistant.apigateway.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final SecretKey key;

	public JwtUtil(@Value("${jwt.secret}") String secret) {
		super();
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
	
	public Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(this.key).build().parseSignedClaims(token).getPayload();
	}
	
	// Validate that the token is not expired and signature matches
	public boolean isTokenValid(String token) {
		try {
			Claims claims = extractAllClaims(token);
			return !claims.getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}
	
	
	
}
