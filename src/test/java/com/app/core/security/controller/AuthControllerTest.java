package com.app.core.security.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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
		LoginDto loginDto = LoginDto.builder().username("gianValentin").password("12345678").build();
		AuthDto authDto = AuthDto.builder().accessToken("access-token").refreshToken("refresh-token").build();

		Mockito.when(authService.login(Mockito.any(LoginDto.class))).thenReturn(authDto);

		mockMvc.perform(post(path.concat("/login"))
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginDto)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value("access-token"));
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
		mockMvc.perform(post(path.concat("/refresh-token")).with(csrf()))
				.andExpect(status().isOk());

		Mockito.verify(authService).refreshToken(Mockito.any(), Mockito.any());
	}
}
