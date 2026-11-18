package com.app.core.security.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.core.security.dto.AuthDto;
import com.app.core.security.dto.LoginDto;
import com.app.core.security.dto.RegisterDto;
import com.app.core.security.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private static final String REFRESH_COOKIE = "refresh_token";
	private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";
	
	private final AuthService authService;
	@Value("${application.security.jwt.refresh-token.expiration}")
	private long refreshExpiration;
	@Value("${security.auth-cookie-secure:true}")
	private boolean secureCookie;
	@Value("${security.auth-cookie-same-site:Strict}")
	private String sameSite;
	@Value("${security.allowedOrigins:http://localhost:4200}")
	private List<String> allowedOrigins;
	@Value("${security.registration.enabled:false}")
	private boolean registrationEnabled;
	
	@PostMapping(value="/login")
	public ResponseEntity<AuthDto> login(@Valid @RequestBody LoginDto dto){
		return authResponse(authService.login(dto));
	}
	
	@PostMapping(value="/register")
	public ResponseEntity<AuthDto> register(@Valid @RequestBody RegisterDto dto){
		if (!registrationEnabled) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registration is disabled");
		}
		return authResponse(authService.register(dto));
	}
	
	@PostMapping(value="/refresh-token")
	public ResponseEntity<AuthDto> refreshToken(HttpServletRequest request){
		assertAllowedOrigin(request);
		return authResponse(authService.refreshToken(request));
	}

	@PostMapping(value="/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request){
		assertAllowedOrigin(request);
		authService.logout(request);
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
				.build();
	}

	private ResponseEntity<AuthDto> authResponse(AuthDto auth) {
		ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from(REFRESH_COOKIE, auth.getRefreshToken())
				.httpOnly(true)
				.secure(secureCookie)
				.path(REFRESH_COOKIE_PATH)
				.sameSite(sameSite);
		if (auth.isRemember()) cookie.maxAge(Duration.ofMillis(refreshExpiration));
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.build().toString())
				.body(auth);
	}

	private ResponseCookie clearRefreshCookie() {
		return ResponseCookie.from(REFRESH_COOKIE, "")
				.httpOnly(true)
				.secure(secureCookie)
				.path(REFRESH_COOKIE_PATH)
				.sameSite(sameSite)
				.maxAge(Duration.ZERO)
				.build();
	}

	private void assertAllowedOrigin(HttpServletRequest request) {
		String origin = request.getHeader(HttpHeaders.ORIGIN);
		if (origin == null || !allowedOrigins.contains(origin)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Origin is not allowed");
		}
	}
}
