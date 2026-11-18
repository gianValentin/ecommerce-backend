package com.app.core.security.service;

import com.app.core.security.dto.AuthDto;
import com.app.core.security.dto.LoginDto;
import com.app.core.security.dto.RegisterDto;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

	AuthDto login(LoginDto dto);

	AuthDto register(RegisterDto dto);
	
	AuthDto refreshToken(final HttpServletRequest request);

	void logout(final HttpServletRequest request);
	
	boolean isUsernameValid(String username);
}
