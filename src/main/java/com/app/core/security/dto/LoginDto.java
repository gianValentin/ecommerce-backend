package com.app.core.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
	@NotBlank(message = "Username no puede estar vacio.")
	@Size(min = 8, max = 20, message = "El rango de caracteres para username es minimo 8 y maximo 20. ")
	private String username;
	@NotBlank(message = "Contraseña no puede estar vacio.")
	@Size(min = 8, max = 20, message = "El rango de caracteres para contraseña es minimo 8 y maximo 20. ")
	private String password;
}
