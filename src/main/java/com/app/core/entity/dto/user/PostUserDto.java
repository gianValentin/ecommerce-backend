package com.app.core.entity.dto.user;

import com.app.core.utils.Constant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostUserDto {
	@NotBlank(message = "{user.name.notblank}")
	@Size(min = 2, max = 32, message = "{user.name.size}")
	String firstname;
	@NotBlank(message = "{user.username.notblank}")
	@Size(min = 2, max = 32, message = "{user.username.size")
	String username;
	@NotBlank(message = "{user.password.notblank}")
	@Size(min = 2, max = 20, message = "{user.password.size}")
	String password;
	@NotBlank(message = "{user.lastname.notblank}")
	@Size(min = 2, max = 32, message = "{user.lastname.size")
	String lastname;
	@NotEmpty(message = "{user.email.notempty}")
	@Email(message = "{user.email.notformat}", regexp = Constant.EMAIL_REGEXP)
	String email;
}
