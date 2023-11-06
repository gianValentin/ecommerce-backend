package com.app.core.security.dto;

import com.app.core.security.annotation.UsernameExistsConstraint;
import com.app.core.utils.Constant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
	@UsernameExistsConstraint(message = "El nombre de usuario  ya existe.")
	@NotBlank(message = "Nombre de usuario no puede estar vacio.")
	@Size(min = 8, max = 20, message = "El rango de caracteres para el nombre de usuario es minimo 8 y maximo 20. ")
	private String username;
	@NotBlank(message = "Contraseña no puede estar vacio.")
	@Size(min = 8, max = 20, message = "El rango de caracteres para contraseña es minimo 8 y maximo 20. ")
	private String password;
	@NotBlank(message = "Firstname no puede estar vacio.")
	@Size(min = 2, max = 32, message = "El rango de caracteres para firstname es minimo 8 y maximo 20. ")
	private String firstname;
	@NotBlank(message = "Lastname no puede estar vacio.")
	@Size(min = 2, max = 32, message = "El rango de caracteres para lastname es minimo 8 y maximo 20. ")
	private String lastname;
	@NotEmpty(message = "Correo no puede estar vacio")
	@Email(message = "El formato para el correo no es válido", regexp = Constant.EMAIL_REGEXP)
	private String email;
}
