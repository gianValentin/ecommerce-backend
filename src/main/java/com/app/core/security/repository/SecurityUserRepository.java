package com.app.core.security.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.core.security.entity.SecurityUser;

public interface SecurityUserRepository  extends JpaRepository<SecurityUser, UUID>{	
	Optional<SecurityUser> findByUsername(final String name);
	Optional<SecurityUser> findByEmail(final String name);
	Optional<SecurityUser> findByUsernameOrEmail(final String name, final String email);
	Optional<SecurityUser> findByLastnameOrEmail(final String name, final String email);
	boolean existsSecurityUserByUsername(final String username);
}
