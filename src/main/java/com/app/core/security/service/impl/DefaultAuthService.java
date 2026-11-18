package com.app.core.security.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.JwtException;

import com.app.core.security.dto.AuthDto;
import com.app.core.security.dto.LoginDto;
import com.app.core.security.dto.RegisterDto;
import com.app.core.security.entity.SecurityUser;
import com.app.core.security.entity.Token;
import com.app.core.security.entity.TokenType;
import com.app.core.security.repository.SecurityUserRepository;
import com.app.core.security.repository.TokenRepository;
import com.app.core.security.service.AuthService;
import com.app.core.security.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {
	
	private final SecurityUserRepository securityUserRepository;
	private final TokenRepository tokenRepository;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final ModelMapper modelMapper;

	@Override
	@Transactional
	public AuthDto login(LoginDto dto) {
		
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
		
		SecurityUser user = securityUserRepository.findByUsernameOrEmail(dto.getUsername(), dto.getUsername()).orElseThrow();
		String token = jwtService.getToken(user);
		String refreshtoken = jwtService.getRefreshToken(user, dto.isRemember());
		
		revokeAllUserTokens(user);
		saveUserTokens(user, token, refreshtoken);
		
		return AuthDto.builder()
				.accessToken(token)
				.username(user.getUsername())
				.role(user.getRole().name())
				.refreshToken(refreshtoken)
				.remember(dto.isRemember())
				.build();
	}

	@Override
	@Transactional
	public AuthDto register(RegisterDto dto) {		
		
		dto.setPassword(passwordEncoder.encode(dto.getPassword()));
		SecurityUser user = modelMapper.map(dto, SecurityUser.class);
		user.setRole(com.app.core.security.entity.Role.USER);
		
		SecurityUser userSaved = securityUserRepository.save(user);
		String token = jwtService.getToken(userSaved);
		String refreshtoken = jwtService.getRefreshToken(userSaved, false);
				
		saveUserTokens(userSaved, token, refreshtoken);
		
		return AuthDto.builder()
				.accessToken(token)
				.username(userSaved.getUsername())
				.role(userSaved.getRole().name())
				.refreshToken(refreshtoken)
				.remember(false)
				.build();
	}
	
	private void saveUserTokens(SecurityUser user, String accessToken, String refreshToken) {
	    Token token = Token.builder()
	        .user(user)
	        .token(accessToken)
	        .tokenType(TokenType.BEARER)
	        .expired(false)
	        .revoked(false)
	        .build();
	    tokenRepository.save(token);
	    Token refresh = Token.builder()
	        .user(user)
	        .token(hashToken(refreshToken))
	        .tokenType(TokenType.REFRESH)
	        .expired(false)
	        .revoked(false)
	        .build();
	    tokenRepository.save(refresh);
	  }
	
	private void revokeAllUserTokens(SecurityUser user) {
	    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
	    if (validUserTokens.isEmpty()) {
	    	return;	
	    }
	    
	    validUserTokens.forEach(token -> {
	      token.setExpired(true);
	      token.setRevoked(true);
	    });
	    tokenRepository.saveAll(validUserTokens);
	  }

	@Override
	@Transactional
	public AuthDto refreshToken(HttpServletRequest request) {
		String refreshToken = getCookie(request, "refresh_token");
		if (refreshToken == null) throw unauthorized();

		try {
			String username = jwtService.extractUsernameFromToken(refreshToken);
			SecurityUser user = securityUserRepository.findByUsernameOrEmail(username, username).orElseThrow(DefaultAuthService::unauthorized);
			tokenRepository.findActiveRefreshTokenForUpdate(hashToken(refreshToken))
					.orElseThrow(DefaultAuthService::unauthorized);
			if (!jwtService.isRefreshToken(refreshToken) || !jwtService.isTokenValid(refreshToken, user)) {
				throw unauthorized();
			}

			boolean remember = jwtService.isRememberMeToken(refreshToken);
			String accessToken = jwtService.getToken(user);
			String rotatedRefreshToken = jwtService.getRefreshToken(user, remember);
			revokeAllUserTokens(user);
			saveUserTokens(user, accessToken, rotatedRefreshToken);
			return AuthDto.builder()
					.accessToken(accessToken)
					.username(user.getUsername())
					.role(user.getRole().name())
					.refreshToken(rotatedRefreshToken)
					.remember(remember)
					.build();
		} catch (ResponseStatusException exception) {
			throw exception;
		} catch (JwtException | IllegalArgumentException exception) {
			throw unauthorized();
		}
	}

	@Override
	@Transactional
	public void logout(HttpServletRequest request) {
		String refreshToken = getCookie(request, "refresh_token");
		Token storedRefresh = refreshToken == null ? null : tokenRepository.findByToken(hashToken(refreshToken)).orElse(null);
		String authHeader = request.getHeader("Authorization");
		Token storedAccess = authHeader != null && authHeader.startsWith("Bearer ")
				? tokenRepository.findByToken(authHeader.substring(7)).orElse(null)
				: null;
		if (storedRefresh != null && (storedRefresh.getTokenType() != TokenType.REFRESH
				|| storedRefresh.isExpired() || storedRefresh.isRevoked())) storedRefresh = null;
		if (storedAccess != null && (storedAccess.getTokenType() != TokenType.BEARER
				|| storedAccess.isExpired() || storedAccess.isRevoked())) storedAccess = null;
		SecurityUser user = storedRefresh != null ? storedRefresh.getUser()
					: storedAccess != null ? storedAccess.getUser() : null;
		if (user != null) revokeAllUserTokens(user);
	}

	private String getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) return null;
		for (var cookie : request.getCookies()) {
			if (name.equals(cookie.getName())) return cookie.getValue();
		}
		return null;
	}

	private String hashToken(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}

	private static ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isUsernameValid(String username) {		
		return !securityUserRepository.existsSecurityUserByUsername(username);
	}

}
