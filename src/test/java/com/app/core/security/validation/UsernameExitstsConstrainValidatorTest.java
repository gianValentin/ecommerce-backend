package com.app.core.security.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.app.core.security.service.AuthService;

import jakarta.validation.ConstraintValidatorContext;

public class UsernameExitstsConstrainValidatorTest {

	private AuthService authService;
	private UsernameExitstsConstrainValidator validator;
	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		authService = mock(AuthService.class);
		context = mock(ConstraintValidatorContext.class);
		validator = new UsernameExitstsConstrainValidator(authService);
	}

	@Test
	@DisplayName("Is valid case username is available")
	public void isValidCaseUsernameIsAvailable() {
		Mockito.when(authService.isUsernameValid("gianValentin")).thenReturn(true);

		assertTrue(validator.isValid("gianValentin", context));
	}

	@Test
	@DisplayName("Is valid case username already exists")
	public void isValidCaseUsernameAlreadyExists() {
		Mockito.when(authService.isUsernameValid("gianValentin")).thenReturn(false);

		assertFalse(validator.isValid("gianValentin", context));
	}

	@Test
	@DisplayName("Is valid case authService throws exception should return false")
	public void isValidCaseAuthServiceThrowsExceptionShouldReturnFalse() {
		Mockito.when(authService.isUsernameValid("gianValentin")).thenThrow(new RuntimeException("db error"));

		assertFalse(validator.isValid("gianValentin", context));
	}
}
