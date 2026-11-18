package com.app.core.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthDto {
	@JsonProperty("access_token")
	private String accessToken;
	private String username;
	private String role;
	@JsonIgnore
	private String refreshToken;
	@JsonIgnore
	private boolean remember;
}
