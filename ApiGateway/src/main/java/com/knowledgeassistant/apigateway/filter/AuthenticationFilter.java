package com.knowledgeassistant.apigateway.filter;


import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;          // ✅ Correct import
import org.springframework.http.HttpStatus;           // ✅ Needed
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.knowledgeassistant.apigateway.util.JwtUtil;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
 
	private final JwtUtil jwtUtil;
	private final WebClient.Builder webClientBuilder;

	public AuthenticationFilter(JwtUtil jwtUtil, WebClient.Builder webClientBuilder) {
		super();
		this.jwtUtil = jwtUtil;
		this.webClientBuilder = webClientBuilder;
	}
	
	private static final List<String> OPEN_ENDPOINTS = List.of("/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/eureka");

	@Override
	public int getOrder() {
		return -1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		
		ServerHttpRequest req = exchange.getRequest();
		String path = req.getURI().getPath();
		
		 // If path is an open public endpoint, let it pass directly
		if(OPEN_ENDPOINTS.stream().anyMatch(path::startsWith))
		return chain.filter(exchange);
		
		//  Check if Authorization header is present
		if (!req.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
        }
		
		String authHeader = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if(authHeader==null || !authHeader.startsWith("Bearer ")) {
			return onError(exchange, "Token is expired or invalid", HttpStatus.UNAUTHORIZED);
		}
		
		// validate token
		
		String token = authHeader.substring(7).trim();
		if(!jwtUtil.isTokenValid(token)) {
			return onError(exchange, "Token is expired or invalid", HttpStatus.UNAUTHORIZED);
		}
		
		// Extract claims
		Claims claims = jwtUtil.extractAllClaims(token);
		String userName = claims.get("username") != null ? claims.get("username").toString() : claims.getSubject();
		String userId = claims.get("userId") != null ? claims.get("userId").toString() : "";
		String role = claims.get("role") != null ? claims.get("role").toString() : "";
		
		//Mutated request
		
		ServerHttpRequest mutatedRequest = exchange.getRequest().mutate().header("X-User-Name", userName).header("X-User-Id",userId)
				.header("X-User-Role", role).build();
		
		return webClientBuilder.build()
				.post()
				.uri("lb://AUTHSERVICE/api/auth/blacklist/check")
				.bodyValue(Map.of("token", token))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Boolean>>() {})
				.flatMap(res -> {
					if (Boolean.TRUE.equals(res.get("blacklisted"))) {
						return onError(exchange, "Token has been revoked. Please log in again.", HttpStatus.UNAUTHORIZED);
					}
					return chain.filter(exchange.mutate().request(mutatedRequest).build());
				})
				.onErrorResume(ex -> onError(exchange, "Authentication service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
		
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		byte[] bytes = ("{\"error\": \"" + err + "\"}").getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = response.bufferFactory().wrap(bytes);
		return response.writeWith(Mono.just(buffer));
	}
	
	
	
}
