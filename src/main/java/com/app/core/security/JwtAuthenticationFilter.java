package com.app.core.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.core.security.repository.TokenRepository;
import com.app.core.security.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.JwtException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	final static String BEARER_SPACE = "Bearer ";

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final TokenRepository tokenRepository;

	@Override
	protected void doFilterInternal(
			@NonNull  HttpServletRequest request,
			@NonNull  HttpServletResponse response,
			@NonNull  FilterChain filterChain)
			throws ServletException, IOException {
		
		// TODO obviar peticiones con tolen y rutas no protegidas
		
		if (request.getServletPath().contains("/api/v1/auth")) {
			filterChain.doFilter(request, response);
			return;
		}

		final String jwt = getTokenFromRequest(request);
		final String username;

		if (jwt == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			username = jwtService.extractUsernameFromToken(jwt);
			if (!jwtService.isAccessToken(jwt)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token");
				return;
			}

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				boolean isTokenValid = tokenRepository.findByToken(jwt)
						.map(token -> token.getTokenType() == com.app.core.security.entity.TokenType.BEARER
								&& !token.isExpired() && !token.isRevoked())
						.orElse(false);

				if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
							userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
			SecurityContextHolder.clearContext();
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token");
			return;
		}

		filterChain.doFilter(request, response);

	}

	private String getTokenFromRequest(HttpServletRequest request) {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_SPACE)) {
			return authHeader.replace(BEARER_SPACE, "");
		}

		return null;
	}

}
