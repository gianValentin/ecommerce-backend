package com.app.core.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.core.security.dto.AuthDto;
import com.app.core.security.dto.LoginDto;
import com.app.core.security.dto.RegisterDto;
import com.app.core.security.entity.Role;
import com.app.core.security.entity.SecurityUser;
import com.app.core.security.entity.Token;
import com.app.core.security.repository.SecurityUserRepository;
import com.app.core.security.repository.TokenRepository;
import com.app.core.security.service.impl.DefaultAuthService;

public class DefaultAuthServiceTest {

	private static final String USERNAME = "gianValentin";
	private static final String ACCESS_TOKEN = "access-token";
	private static final String REFRESH_TOKEN = "refresh-token";

	private SecurityUserRepository securityUserRepository;
	private TokenRepository tokenRepository;
	private JwtService jwtService;
	private PasswordEncoder passwordEncoder;
	private AuthenticationManager authenticationManager;
	private ModelMapper modelMapper;
	private DefaultAuthService authService;

	private SecurityUser user;

	@BeforeEach
	void setUp() {
		securityUserRepository = mock(SecurityUserRepository.class);
		tokenRepository = mock(TokenRepository.class);
		jwtService = mock(JwtService.class);
		passwordEncoder = mock(PasswordEncoder.class);
		authenticationManager = mock(AuthenticationManager.class);
		modelMapper = mock(ModelMapper.class);
		authService = new DefaultAuthService(securityUserRepository, tokenRepository, jwtService, passwordEncoder,
				authenticationManager, modelMapper);

		user = SecurityUser.builder().id(UUID.randomUUID()).username(USERNAME).role(Role.USER).build();
	}

	@Test
	@DisplayName("Login case success should authenticate and return access and refresh token")
	public void loginCaseSuccessShouldAuthenticateAndReturnTokens() {
		LoginDto dto = LoginDto.builder().username(USERNAME).password("12345678").build();

		Mockito.when(securityUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		Mockito.when(jwtService.getToken(user)).thenReturn(ACCESS_TOKEN);
		Mockito.when(jwtService.getRefreshToken(user)).thenReturn(REFRESH_TOKEN);
		Mockito.when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(List.of());

		AuthDto authDto = authService.login(dto);

		assertNotNull(authDto);
		assertEquals(ACCESS_TOKEN, authDto.getAccessToken());
		assertEquals(REFRESH_TOKEN, authDto.getRefreshToken());
		verify(authenticationManager).authenticate(any());
		verify(tokenRepository).save(any(Token.class));
	}

	@Test
	@DisplayName("Login case user not found should throw exception")
	public void loginCaseUserNotFoundShouldThrowException() {
		LoginDto dto = LoginDto.builder().username(USERNAME).password("12345678").build();

		Mockito.when(securityUserRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> authService.login(dto));
	}

	@Test
	@DisplayName("Login case success should revoke previous valid tokens")
	public void loginCaseSuccessShouldRevokePreviousValidTokens() {
		LoginDto dto = LoginDto.builder().username(USERNAME).password("12345678").build();
		Token oldToken = Token.builder().token("old-token").expired(false).revoked(false).build();

		Mockito.when(securityUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		Mockito.when(jwtService.getToken(user)).thenReturn(ACCESS_TOKEN);
		Mockito.when(jwtService.getRefreshToken(user)).thenReturn(REFRESH_TOKEN);
		Mockito.when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(List.of(oldToken));

		authService.login(dto);

		assertEquals(true, oldToken.isExpired());
		assertEquals(true, oldToken.isRevoked());
		verify(tokenRepository).saveAll(anyList());
	}

	@Test
	@DisplayName("Register case success should encode password and return tokens")
	public void registerCaseSuccessShouldEncodePasswordAndReturnTokens() {
		RegisterDto dto = RegisterDto.builder()
				.username(USERNAME)
				.password("12345678")
				.firstname("giancarlo")
				.lastname("valentin")
				.email("giancarlo@valentin.com")
				.build();

		Mockito.when(passwordEncoder.encode("12345678")).thenReturn("encoded-password");
		Mockito.when(modelMapper.map(dto, SecurityUser.class)).thenReturn(user);
		Mockito.when(securityUserRepository.save(user)).thenReturn(user);
		Mockito.when(jwtService.getToken(user)).thenReturn(ACCESS_TOKEN);
		Mockito.when(jwtService.getRefreshToken(user)).thenReturn(REFRESH_TOKEN);

		AuthDto authDto = authService.register(dto);

		assertEquals("encoded-password", dto.getPassword());
		assertEquals(ACCESS_TOKEN, authDto.getAccessToken());
		assertEquals(REFRESH_TOKEN, authDto.getRefreshToken());
		verify(tokenRepository).save(any(Token.class));
	}

	@Test
	@DisplayName("Refresh token case no authorization header should not generate a new token")
	public void refreshTokenCaseNoAuthorizationHeaderShouldNotGenerateNewToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		authService.refreshToken(request, response);

		verify(jwtService, never()).getToken(any());
		assertEquals(0, response.getContentAsByteArray().length);
	}

	@Test
	@DisplayName("Refresh token case valid token should write a new access token to the response")
	public void refreshTokenCaseValidTokenShouldWriteNewAccessTokenToResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + REFRESH_TOKEN);
		MockHttpServletResponse response = new MockHttpServletResponse();

		Mockito.when(jwtService.extractUsernameFromToken(REFRESH_TOKEN)).thenReturn(USERNAME);
		Mockito.when(securityUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
		Mockito.when(jwtService.isTokenValid(REFRESH_TOKEN, user)).thenReturn(true);
		Mockito.when(jwtService.getToken(user)).thenReturn(ACCESS_TOKEN);
		Mockito.when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(List.of());

		authService.refreshToken(request, response);

		verify(tokenRepository, times(1)).save(any(Token.class));
		assertEquals("application/json", response.getContentType());
		String body = response.getContentAsString();
		assertEquals(true, body.contains(ACCESS_TOKEN));
	}

	@Test
	@DisplayName("Is username valid case username already exists")
	public void isUsernameValidCaseUsernameAlreadyExists() {
		Mockito.when(securityUserRepository.existsSecurityUserByUsername(USERNAME)).thenReturn(true);

		assertEquals(false, authService.isUsernameValid(USERNAME));
	}

	@Test
	@DisplayName("Is username valid case username available")
	public void isUsernameValidCaseUsernameAvailable() {
		Mockito.when(securityUserRepository.existsSecurityUserByUsername(USERNAME)).thenReturn(false);

		assertEquals(true, authService.isUsernameValid(USERNAME));
	}
}
