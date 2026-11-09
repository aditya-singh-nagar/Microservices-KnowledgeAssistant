package com.knowledgeassistant.authservice.utility;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTUtility {

    @Value("${jwt.secret:${secret:}}")
    private String secretKey ;

    private static final long ACCESS_TOKEN_VALIDITY = 15*60*1000;
    
    public SecretKey getKey () {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, String email, List<String> roles) {

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // Primary role (for single-role checks like ApiGateway claims.get("role"))
        String primaryRole = (roles != null && !roles.isEmpty()) ? roles.get(0) : "ROLE_PUBLIC";
        claims.put("role", primaryRole);

        // Full list of roles for multiple-role authorizations
        claims.put("roles", roles);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(getKey())
                .compact();

    }
    
    public Claims extractAllClaims(String token) {
    	
    	return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token, String userEmail) {
        try {
            String email = extractEmail(token);
            return email != null && !isTokenExpired(token) && email.equals(userEmail);
        } catch (Exception e) {
            return false;
        }
    }
    
    
    public Date extractExpiration(String token) {
    	return extractAllClaims(token).getExpiration();
    }




}
