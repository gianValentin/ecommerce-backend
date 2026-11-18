package com.app.core.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.app.core.security.entity.Role;

class RoleAuthoritiesTest {

	@Test
	void adminReceivesRoleAndDeclaredPermissions() {
		var authorities = Role.ADMIN.getAuthorities().stream()
				.map(authority -> authority.getAuthority())
				.toList();

		assertTrue(authorities.contains("ROLE_ADMIN"));
		assertTrue(authorities.contains("admin:create"));
		assertTrue(authorities.contains("admin:update"));
		assertTrue(authorities.contains("admin:delete"));
	}

	@Test
	void regularUserDoesNotReceiveAdministrativeAuthorities() {
		var authorities = Role.USER.getAuthorities().stream()
				.map(authority -> authority.getAuthority())
				.toList();

		assertTrue(authorities.contains("ROLE_USER"));
		assertFalse(authorities.contains("ROLE_ADMIN"));
		assertFalse(authorities.contains("admin:create"));
	}
}
