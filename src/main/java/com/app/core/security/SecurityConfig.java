package com.app.core.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.springframework.http.HttpMethod.*;

import static com.app.core.security.entity. Role.ADMIN;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String[] WHITE_LIST_URL = {
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
			/*Auth*/
			"/api/v1/auth/**",
			/*Cart*/
			"/api/v1/cart/**"
			};

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final AuthenticationProvider authenticationProvider;
	private final LogoutHandler logoutHandler;
	
	@Value("${security.allowedOrigins}")
	private final List<String> allowedOrigins;

	@Bean
	@Profile("h2")
	public MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
		return new MvcRequestMatcher.Builder(introspector);
	}

	@Bean
	@Profile("h2")
	SecurityFilterChain h2SecurityFilterChain(MvcRequestMatcher.Builder mvc, HttpSecurity httpSecurity) throws Exception {
		return httpSecurity.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authResquest -> {

					Arrays.stream(WHITE_LIST_URL).forEach(url -> authResquest.requestMatchers(mvc.pattern(url)).permitAll());

					authResquest.requestMatchers(PathRequest.toH2Console()).permitAll();

					authResquest.requestMatchers(mvc.pattern(GET, "/api/v1/product/**")).permitAll();
					authResquest.requestMatchers(mvc.pattern(GET, "/api/v1/categories/**")).permitAll();

					authResquest.requestMatchers(mvc.pattern(POST, "/api/v1/product/**")).hasAnyAuthority(ADMIN.name());
					authResquest.requestMatchers(mvc.pattern(PUT, "/api/v1/product/**")).hasAnyAuthority(ADMIN.name());
					authResquest.requestMatchers(mvc.pattern(DELETE, "/api/v1/product/**")).hasAnyAuthority(ADMIN.name());
					authResquest.requestMatchers(mvc.pattern(POST, "/api/v1/categories/**")).hasAnyAuthority(ADMIN.name());

					authResquest.requestMatchers(mvc.pattern(GET, "/api/v1/user/session")).permitAll();
					authResquest.requestMatchers(mvc.pattern(GET, "/api/v1/user/**")).hasAnyAuthority(ADMIN.name());
					authResquest.requestMatchers(mvc.pattern(POST, "/api/v1/user/**")).hasAnyAuthority(ADMIN.name());
					authResquest.requestMatchers(mvc.pattern(PUT, "/api/v1/user/**")).hasAnyAuthority(ADMIN.name());
					authResquest.requestMatchers(mvc.pattern(DELETE, "/api/v1/user/**")).hasAnyAuthority(ADMIN.name());

					authResquest.anyRequest().authenticated();
				}).sessionManagement(sessionManagement ->
						sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authenticationProvider(authenticationProvider)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.logout(logout -> {
					logout.logoutUrl("/api/v1/auth/logout");
					logout.addLogoutHandler(logoutHandler);
					logout.logoutSuccessHandler(
							(request, response, authentication) -> SecurityContextHolder.clearContext());
				})
				.headers(a -> a.frameOptions(frame -> frame.disable()))
				.cors(Customizer.withDefaults())
				.build();
	}

	@Bean
	@Profile("postgresql")
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authResquest -> {
			authResquest.requestMatchers(WHITE_LIST_URL).permitAll();
			
			authResquest.requestMatchers(GET,"/api/v1/product/**").permitAll();
			authResquest.requestMatchers(GET,"/api/v1/categories/**").permitAll();			
						
			 authResquest.requestMatchers(POST, "/api/v1/product/**").hasAnyAuthority(ADMIN.name());
			 authResquest.requestMatchers(PUT, "/api/v1/product/**").hasAnyAuthority(ADMIN.name());
			 authResquest.requestMatchers(DELETE, "/api/v1/product/**").hasAnyAuthority(ADMIN.name());			 
			 authResquest.requestMatchers(POST, "/api/v1/categories/**").hasAnyAuthority(ADMIN.name());
			 
			 authResquest.requestMatchers(GET, "/api/v1/user/session").permitAll();
			 authResquest.requestMatchers(GET, "/api/v1/user/**").hasAnyAuthority(ADMIN.name());
			 authResquest.requestMatchers(POST, "/api/v1/user/**").hasAnyAuthority(ADMIN.name());
			 authResquest.requestMatchers(PUT, "/api/v1/user/**").hasAnyAuthority(ADMIN.name());
			 authResquest.requestMatchers(DELETE, "/api/v1/user/**").hasAnyAuthority(ADMIN.name());
			 
			authResquest.anyRequest().authenticated();
		}).sessionManagement(sessionManagement ->
						sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authenticationProvider(authenticationProvider)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.logout(logout -> {
					logout.logoutUrl("/api/v1/auth/logout");
					logout.addLogoutHandler(logoutHandler);
					logout.logoutSuccessHandler(
							(request, response, authentication) -> SecurityContextHolder.clearContext());
				})
				 .cors(Customizer.withDefaults())
				.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource( ) {
		CorsConfiguration configuration = new CorsConfiguration();		
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PATCH.name(), PUT.name(), DELETE.name(), OPTIONS.name(), HEAD.name()));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Requestor-Type","Content-Type"));
		configuration.setExposedHeaders(Arrays.asList("X-Get-Header"));		
		configuration.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
