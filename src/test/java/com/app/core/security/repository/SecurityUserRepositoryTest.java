package com.app.core.security.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import com.app.core.security.entity.Role;
import com.app.core.security.entity.SecurityUser;

/**
 * Usa el datasource definido por el perfil Maven activo (h2 o postgresql),
 * tal como lo hace la aplicacion en runtime. Ejecutar con:
 * ./mvnw test -Ph2 (default) o ./mvnw test -Ppostgresql
 */
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SecurityUserRepositoryTest {

	private static final String USERNAME = "gianValentin";

	@Autowired
	private SecurityUserRepository securityUserRepository;
	@Autowired
	private TestEntityManager testEntityManager;

	@BeforeEach
	void setUp() {
		securityUserRepository.deleteAll();
		testEntityManager.persist(SecurityUser.builder()
				.username(USERNAME)
				.password("1234")
				.firstname("giancarlo")
				.lastname("valentin")
				.email("giancarlo@valentin.com")
				.role(Role.USER)
				.build());
	}

	@Test
	@DisplayName("Find by username case found")
	public void findByUsernameCaseFound() {
		Optional<SecurityUser> userDb = securityUserRepository.findByUsername(USERNAME);

		assertTrue(userDb.isPresent());
		assertThat(userDb.get().getUsername()).isEqualTo(USERNAME);
	}

	@Test
	@DisplayName("Find by username case not found")
	public void findByUsernameCaseNotFound() {
		Optional<SecurityUser> userDb = securityUserRepository.findByUsername("unknown");

		assertTrue(userDb.isEmpty());
	}

	@Test
	@DisplayName("Exists security user by username case true")
	public void existsSecurityUserByUsernameCaseTrue() {
		assertTrue(securityUserRepository.existsSecurityUserByUsername(USERNAME));
	}

	@Test
	@DisplayName("Exists security user by username case false")
	public void existsSecurityUserByUsernameCaseFalse() {
		assertFalse(securityUserRepository.existsSecurityUserByUsername("unknown"));
	}

	@Test
	@DisplayName("Save security user case success")
	public void saveSecurityUserCaseSuccess() {
		SecurityUser newUser = SecurityUser.builder()
				.username("mariaCasas")
				.password("1234")
				.firstname("maria")
				.lastname("casas")
				.email("maria@casas.com")
				.role(Role.USER)
				.build();

		SecurityUser savedUser = securityUserRepository.save(newUser);

		assertThat(savedUser).isNotNull();
		assertThat(savedUser.getId()).isNotNull();
	}
}
