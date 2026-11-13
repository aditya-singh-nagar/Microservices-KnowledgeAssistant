package com.knowledgeassistant.authservice.service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenBlackListService {

	private final Map<String, Date> blackListedToken = new ConcurrentHashMap<>();

	public void blackListedToken(String token, Date expirationDate) {
		if (token != null && expirationDate != null) {
			blackListedToken.put(token, expirationDate);
		}
	}

	public boolean isBlackListed(String token) {
		if (token == null) {
			return false;
		}
		Date expiry = blackListedToken.get(token);
		if (expiry == null) {
			return false;
		}
		if (expiry.before(new Date())) {
			blackListedToken.remove(token);
			return false;
		}
		return true;
	}

	@Scheduled(fixedRate = 600000)
	public void purgeExpiredToken() {
		Date now = new Date();
		blackListedToken.entrySet().removeIf(entry -> entry.getValue().before(now));
	}
}
