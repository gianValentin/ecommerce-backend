package com.app.core.security.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import com.app.core.entity.model.UserModel;
import com.app.core.security.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class DefaultJwtService implements JwtService {
	
	@Value("${application.security.jwt.secret-key}")
	  private String SECRET_KEY;
	  @Value("${application.security.jwt.expiration}")
	  private long jwtExpiration;
	@Value("${application.security.jwt.refresh-token.expiration}")
	  private long refreshExpiration;
	  @Value("${application.security.jwt.issuer:ecommerce-api}")
	  private String issuer = "ecommerce-api";
	  @Value("${application.security.jwt.audience:ecommerce-admin}")
	  private String audience = "ecommerce-admin";

	@PostConstruct
	void validateConfiguration() {
		byte[] key = Decoders.BASE64.decode(SECRET_KEY);
		if (key.length < 32) {
			throw new IllegalStateException("JWT_SECRET_KEY must be a Base64-encoded key of at least 256 bits");
		}
		if (jwtExpiration <= 0 || refreshExpiration <= jwtExpiration) {
			throw new IllegalStateException("Refresh token expiration must exceed the positive access token expiration");
		}
	}
	
	@Override
	public String extractUsernameFromToken(String token) {
		return getClaim(token, Claims::getSubject);
	}
	
	public <T> T getClaim(String token, Function<Claims, T> clamsResolver) {
		final Claims claims = extractAllClaims(token);
		return clamsResolver.apply(claims);
	}
	
	private Claims extractAllClaims(String token) {
		return Jwts
				.parserBuilder()
				.setSigningKey(getSignInKey())
				.requireIssuer(issuer)
				.requireAudience(audience)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	public String getToken(UserModel userModel) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("token_type", "access");
		claims.put("role", userModel.getRole().name());
		return getToken(claims, userModel, jwtExpiration);
	}
	
	@Override
	public String getRefreshToken(
			UserModel userModel
	) {
		return getRefreshToken(userModel, false);
	}

	@Override
	public String getRefreshToken(UserModel userModel, boolean remember) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("token_type", "refresh");
		claims.put("remember_me", remember);
		return getToken(claims, userModel, refreshExpiration);
	}

	@Override
	public boolean isAccessToken(String token) {
		return "access".equals(getClaim(token, claims -> claims.get("token_type", String.class)));
	}

	@Override
	public boolean isRefreshToken(String token) {
		return "refresh".equals(getClaim(token, claims -> claims.get("token_type", String.class)));
	}

	@Override
	public boolean isRememberMeToken(String token) {
		return Boolean.TRUE.equals(getClaim(token, claims -> claims.get("remember_me", Boolean.class)));
	}
	
	private String getToken(
			Map<String, Object> extraClains, 
			UserModel userModel,
			long expiration) {		
		return Jwts
				.builder()
				.setClaims(extraClains)
				.setSubject(userModel.getUsername())
				.setId(UUID.randomUUID().toString())
				.setIssuer(issuer)
				.setAudience(audience)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis()+expiration))
				.signWith(getSignInKey(), SignatureAlgorithm.HS256)
				.compact();
	}	

	@Override
	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	
	private boolean isTokenExpired(String token) {
		return getExpiration(token).before(new Date());
	}
	
	private Date getExpiration(String token) {
		return getClaim(token, Claims::getExpiration);
	}

}
