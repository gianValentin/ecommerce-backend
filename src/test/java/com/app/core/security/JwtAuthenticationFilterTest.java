package com.app.core.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.app.core.security.entity.Token;
import com.app.core.security.repository.TokenRepository;
import com.app.core.security.service.JwtService;

import jakarta.servlet.FilterChain;

public class JwtAuthenticationFilterTest {

	private static final String USERNAME = "gianValentin";
	private static final String VALID_JWT = "valid-jwt";

	private JwtService jwtService;
	private UserDetailsService userDetailsService;
	private TokenRepository tokenRepository;
	private JwtAuthenticationFilter filter;
	private FilterChain filterChain;

	@BeforeEach
	void setUp() {
		jwtService = mock(JwtService.class);
		userDetailsService = mock(UserDetailsService.class);
		tokenRepository = mock(TokenRepository.class);
		filterChain = mock(FilterChain.class);
		filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Doesn't authenticate and lets the request continue for auth endpoints")
	public void doFilterInternalCaseAuthEndpointSkipsAuthentication() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(jwtService, userDetailsService, tokenRepository);
	}

	@Test
	@DisplayName("Lets the request continue without authenticating when there's no Authorization header")
	public void doFilterInternalCaseNoAuthorizationHeaderContinuesUnauthenticated() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cart/1");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	@DisplayName("Lets the request continue without authenticating when the header doesn't have the Bearer prefix")
	public void doFilterInternalCaseHeaderWithoutBearerPrefixContinuesUnauthenticated() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cart/1");
		request.addHeader("Authorization", VALID_JWT);
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verifyNoInteractions(jwtService);
	}

	@Test
	@DisplayName("Authenticates the user when the token is valid and not revoked nor expired")
	public void doFilterInternalCaseValidTokenAuthenticatesUser() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/user/session");
		request.addHeader("Authorization", "Bearer " + VALID_JWT);
		MockHttpServletResponse response = new MockHttpServletResponse();

		UserDetails userDetails = new User(USERNAME, "1234", List.of());
		Token storedToken = Token.builder().token(VALID_JWT).expired(false).revoked(false).build();

		Mockito.when(jwtService.extractUsernameFromToken(VALID_JWT)).thenReturn(USERNAME);
		Mockito.when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
		Mockito.when(tokenRepository.findByToken(VALID_JWT)).thenReturn(Optional.of(storedToken));
		Mockito.when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(true);

		filter.doFilterInternal(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Doesn't authenticate the user when the stored token is revoked")
	public void doFilterInternalCaseRevokedTokenDoesNotAuthenticate() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/user/session");
		request.addHeader("Authorization", "Bearer " + VALID_JWT);
		MockHttpServletResponse response = new MockHttpServletResponse();

		UserDetails userDetails = new User(USERNAME, "1234", List.of());
		Token storedToken = Token.builder().token(VALID_JWT).expired(false).revoked(true).build();

		Mockito.when(jwtService.extractUsernameFromToken(VALID_JWT)).thenReturn(USERNAME);
		Mockito.when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
		Mockito.when(tokenRepository.findByToken(VALID_JWT)).thenReturn(Optional.of(storedToken));
		Mockito.when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(true);

		filter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Doesn't authenticate the user when the token is not found in the database")
	public void doFilterInternalCaseTokenNotStoredDoesNotAuthenticate() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/user/session");
		request.addHeader("Authorization", "Bearer " + VALID_JWT);
		MockHttpServletResponse response = new MockHttpServletResponse();

		UserDetails userDetails = new User(USERNAME, "1234", List.of());

		Mockito.when(jwtService.extractUsernameFromToken(VALID_JWT)).thenReturn(USERNAME);
		Mockito.when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
		Mockito.when(tokenRepository.findByToken(VALID_JWT)).thenReturn(Optional.empty());

		filter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}
}
