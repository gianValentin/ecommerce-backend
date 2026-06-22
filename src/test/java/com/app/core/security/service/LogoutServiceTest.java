package com.app.core.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.app.core.security.entity.Token;
import com.app.core.security.repository.TokenRepository;
import com.app.core.security.service.impl.LogoutService;

public class LogoutServiceTest {

	private static final String JWT = "valid-jwt";

	private TokenRepository tokenRepository;
	private LogoutService logoutService;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@BeforeEach
	void setUp() {
		tokenRepository = mock(TokenRepository.class);
		logoutService = new LogoutService(tokenRepository);
	}

	@Test
	@DisplayName("Logout case valid token should revoke and expire the stored token")
	public void logoutCaseValidTokenShouldRevokeAndExpireStoredToken() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + JWT);
		MockHttpServletResponse response = new MockHttpServletResponse();
		Token storedToken = Token.builder().token(JWT).expired(false).revoked(false).build();
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", null));

		Mockito.when(tokenRepository.findByToken(JWT)).thenReturn(Optional.of(storedToken));

		logoutService.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

		assertEquals(true, storedToken.isExpired());
		assertEquals(true, storedToken.isRevoked());
		verify(tokenRepository).save(storedToken);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	@DisplayName("Logout case no authorization header should do nothing")
	public void logoutCaseNoAuthorizationHeaderShouldDoNothing() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		logoutService.logout(request, response, null);

		verify(tokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("Logout case token not found should do nothing")
	public void logoutCaseTokenNotFoundShouldDoNothing() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + JWT);
		MockHttpServletResponse response = new MockHttpServletResponse();

		Mockito.when(tokenRepository.findByToken(JWT)).thenReturn(Optional.empty());

		logoutService.logout(request, response, null);

		verify(tokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}
}
