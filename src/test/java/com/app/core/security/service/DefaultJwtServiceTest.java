package com.app.core.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.app.core.security.entity.Role;
import com.app.core.security.entity.SecurityUser;
import com.app.core.security.service.impl.DefaultJwtService;

public class DefaultJwtServiceTest {

	private static final String SECRET_KEY =
			"586E3272357538782F413F4428472B4B6250655368566B597033733676397924";

	private DefaultJwtService jwtService;
	private SecurityUser user;

	@BeforeEach
	void setUp() {
		jwtService = new DefaultJwtService();
		ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET_KEY);
		ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L);
		ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604_800_000L);

		user = SecurityUser.builder()
				.id(UUID.randomUUID())
				.username("gianValentin")
				.password("1234")
				.role(Role.USER)
				.build();
	}

	@Test
	@DisplayName("Get token should generate a token with the username as subject")
	public void getTokenShouldGenerateTokenWithUsernameAsSubject() {
		String token = jwtService.getToken(user);

		assertNotNull(token);
		assertEquals(user.getUsername(), jwtService.extractUsernameFromToken(token));
	}

	@Test
	@DisplayName("Get refresh token should generate a token with a later expiration than the access token")
	public void getRefreshTokenShouldGenerateTokenWithLaterExpiration() {
		String accessToken = jwtService.getToken(user);
		String refreshToken = jwtService.getRefreshToken(user);

		assertNotEquals(accessToken, refreshToken);

		var accessExpiration = jwtService.getClaim(accessToken, Claims::getExpiration);
		var refreshExpiration = jwtService.getClaim(refreshToken, Claims::getExpiration);

		assertTrue(refreshExpiration.after(accessExpiration));
	}

	@Test
	@DisplayName("Is token valid case username matches and token not expired")
	public void isTokenValidCaseUsernameMatchesAndTokenNotExpired() {
		String token = jwtService.getToken(user);

		assertTrue(jwtService.isTokenValid(token, user));
	}

	@Test
	@DisplayName("Is token valid case username does not match")
	public void isTokenValidCaseUsernameDoesNotMatch() {
		String token = jwtService.getToken(user);

		SecurityUser otherUser = SecurityUser.builder()
				.id(UUID.randomUUID())
				.username("otherUser")
				.role(Role.USER)
				.build();

		assertFalse(jwtService.isTokenValid(token, otherUser));
	}

	@Test
	@DisplayName("Is token valid case token expired throws ExpiredJwtException")
	public void isTokenValidCaseTokenExpiredThrowsExpiredJwtException() {
		ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);

		String expiredToken = jwtService.getToken(user);

		assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(expiredToken, user));
	}
}
