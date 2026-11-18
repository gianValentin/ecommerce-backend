package com.app.core.security.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.app.core.config.ValidationConfig;
import com.app.core.security.JwtAuthenticationFilter;
import com.app.core.security.dto.AuthDto;
import com.app.core.security.dto.LoginDto;
import com.app.core.security.dto.RegisterDto;
import com.app.core.security.service.AuthService;
import com.app.core.security.validation.UsernameExitstsConstrainValidator;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
		controllers = AuthController.class,
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = JwtAuthenticationFilter.class))
@Import({ValidationConfig.class, UsernameExitstsConstrainValidator.class})
@TestPropertySource(properties = {
		"security.registration.enabled=true",
		"security.allowedOrigins=http://localhost:4200",
		"security.auth-cookie-secure=false"
})
public class AuthControllerTest {

	private final String path = "/api/v1/auth";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService authService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@WithMockUser
	@DisplayName("Login case success")
	public void loginCaseSuccess() throws Exception {
		LoginDto loginDto = LoginDto.builder().username("gianValentin").password("12345678").remember(true).build();
		AuthDto authDto = AuthDto.builder().accessToken("access-token").refreshToken("refresh-token")
				.username("gianValentin").role("ADMIN").remember(true).build();

		Mockito.when(authService.login(Mockito.any(LoginDto.class))).thenReturn(authDto);

		mockMvc.perform(post(path.concat("/login"))
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginDto)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value("access-token"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.role").value("ADMIN"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.refresh_token").doesNotExist())
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=604800")));

		ArgumentCaptor<LoginDto> capturedLogin = ArgumentCaptor.forClass(LoginDto.class);
		Mockito.verify(authService).login(capturedLogin.capture());
		org.junit.jupiter.api.Assertions.assertTrue(capturedLogin.getValue().isRemember());
	}

	@Test
	@WithMockUser
	@DisplayName("Register case success")
	public void registerCaseSuccess() throws Exception {
		RegisterDto registerDto = RegisterDto.builder()
				.username("gianValentin")
				.password("12345678")
				.firstname("giancarlo")
				.lastname("valentin")
				.email("giancarlo@valentin.com")
				.build();
		AuthDto authDto = AuthDto.builder().accessToken("access-token").refreshToken("refresh-token").build();

		Mockito.when(authService.isUsernameValid("gianValentin")).thenReturn(true);
		Mockito.when(authService.register(Mockito.any(RegisterDto.class))).thenReturn(authDto);

		mockMvc.perform(post(path.concat("/register"))
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerDto)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value("access-token"));
	}

	@Test
	@WithMockUser
	@DisplayName("Register case username already exists returns bad request")
	public void registerCaseUsernameAlreadyExistsReturnsBadRequest() throws Exception {
		RegisterDto registerDto = RegisterDto.builder()
				.username("gianValentin")
				.password("12345678")
				.firstname("giancarlo")
				.lastname("valentin")
				.email("giancarlo@valentin.com")
				.build();

		Mockito.when(authService.isUsernameValid("gianValentin")).thenReturn(false);

		mockMvc.perform(post(path.concat("/register"))
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registerDto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	@DisplayName("Refresh token case success")
	public void refreshTokenCaseSuccess() throws Exception {
		Mockito.when(authService.refreshToken(Mockito.any())).thenReturn(
				AuthDto.builder().accessToken("new-access").refreshToken("new-refresh").remember(true).build());
		mockMvc.perform(post(path.concat("/refresh-token"))
						.with(csrf())
						.header("Origin", "http://localhost:4200")
						.cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh")))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value("new-access"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.refresh_token").doesNotExist())
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("new-refresh")));

		Mockito.verify(authService).refreshToken(Mockito.any());
	}

	@Test
	@WithMockUser
	@DisplayName("Refresh token rejects a request from an untrusted origin")
	void refreshTokenRejectsUntrustedOrigin() throws Exception {
		mockMvc.perform(post(path.concat("/refresh-token"))
					.with(csrf())
					.header("Origin", "https://attacker.example"))
				.andExpect(status().isForbidden());

		Mockito.verifyNoInteractions(authService);
	}

	@Test
	@WithMockUser
	@DisplayName("Login validates credentials before invoking the authentication service")
	void loginRejectsInvalidRequest() throws Exception {
		mockMvc.perform(post(path.concat("/login"))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"short\",\"password\":\"tiny\"}"))
				.andExpect(status().isBadRequest());

		Mockito.verifyNoInteractions(authService);
	}

	@Test
	@WithMockUser
	@DisplayName("Login returns a generic unauthorized response for invalid credentials")
	void loginRejectsInvalidCredentials() throws Exception {
		Mockito.when(authService.login(Mockito.any(LoginDto.class))).thenThrow(new BadCredentialsException("details hidden"));
		LoginDto loginDto = LoginDto.builder().username("gianValentin").password("incorrecta").build();

		mockMvc.perform(post(path.concat("/login"))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(loginDto)))
				.andExpect(status().isUnauthorized())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid username or password"));
	}

	@Test
	@WithMockUser
	@DisplayName("Logout revokes the server session and clears the refresh cookie")
	void logoutClearsRefreshCookie() throws Exception {
		mockMvc.perform(post(path.concat("/logout"))
					.with(csrf())
					.header("Origin", "http://localhost:4200"))
				.andExpect(status().isNoContent())
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

		Mockito.verify(authService).logout(Mockito.any());
	}

	@Test
	@DisplayName("Registration is denied unless explicitly enabled")
	void registrationIsDeniedWhenDisabled() {
		AuthController controller = new AuthController(authService);
		ReflectionTestUtils.setField(controller, "registrationEnabled", false);
		RegisterDto dto = RegisterDto.builder()
				.username("gianValentin")
				.password("12345678")
				.firstname("giancarlo")
				.lastname("valentin")
				.email("giancarlo@valentin.com")
				.build();

		org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class, () -> controller.register(dto));
		Mockito.verifyNoInteractions(authService);
	}
}
