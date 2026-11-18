package com.app.core.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

final class SecurityAuthorizationRules {

	private static final String[] PUBLIC_PATHS = {
			"/v2/api-docs",
			"/v3/api-docs",
			"/v3/api-docs/**",
			"/swagger-resources",
			"/swagger-resources/**",
			"/configuration/ui",
			"/configuration/security",
			"/swagger-ui/**",
			"/webjars/**",
			"/swagger-ui.html",
			"/api/v1/auth/**",
			"/api/v1/cart/**"
	};

	private SecurityAuthorizationRules() {}

	static void configure(HttpSecurity http, boolean h2ConsoleEnabled) throws Exception {
		http.authorizeHttpRequests(authorize -> {
			if (h2ConsoleEnabled) authorize.requestMatchers(PathRequest.toH2Console()).permitAll();

			authorize.requestMatchers(PUBLIC_PATHS).permitAll();
			authorize.requestMatchers(GET, "/api/v1/product/**").permitAll();
			authorize.requestMatchers(GET, "/api/v1/categories/**").permitAll();
			authorize.requestMatchers(GET, "/api/v1/user/session").permitAll();

			authorize.requestMatchers(GET, "/api/v1/menus/**").hasRole("ADMIN");
			authorize.requestMatchers(POST, "/api/v1/product/**").hasRole("ADMIN");
			authorize.requestMatchers(PUT, "/api/v1/product/**").hasRole("ADMIN");
			authorize.requestMatchers(DELETE, "/api/v1/product/**").hasRole("ADMIN");
			authorize.requestMatchers(POST, "/api/v1/categories/**").hasRole("ADMIN");
			authorize.requestMatchers(PUT, "/api/v1/categories/**").hasRole("ADMIN");
			authorize.requestMatchers(DELETE, "/api/v1/categories/**").hasRole("ADMIN");
			authorize.requestMatchers(POST, "/api/v1/menus/**").hasRole("ADMIN");
			authorize.requestMatchers(PUT, "/api/v1/menus/**").hasRole("ADMIN");
			authorize.requestMatchers(DELETE, "/api/v1/menus/**").hasRole("ADMIN");
			authorize.requestMatchers("/api/v1/user/**").hasRole("ADMIN");

			authorize.anyRequest().authenticated();
		});
	}
}
