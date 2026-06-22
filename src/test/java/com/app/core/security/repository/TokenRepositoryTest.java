package com.app.core.security.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
import com.app.core.security.entity.Token;
import com.app.core.security.entity.TokenType;

/**
 * Usa el datasource definido por el perfil Maven activo (h2 o postgresql),
 * tal como lo hace la aplicacion en runtime. Ejecutar con:
 * ./mvnw test -Ph2 (default) o ./mvnw test -Ppostgresql
 */
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TokenRepositoryTest {

	private static final String VALID_TOKEN = "valid-token";
	private static final String REVOKED_TOKEN = "revoked-token";

	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private TestEntityManager testEntityManager;

	private SecurityUser user;

	@BeforeEach
	void setUp() {
		tokenRepository.deleteAll();

		user = testEntityManager.persist(SecurityUser.builder()
				.username("gianValentin")
				.password("1234")
				.firstname("giancarlo")
				.lastname("valentin")
				.email("giancarlo@valentin.com")
				.role(Role.USER)
				.build());

		testEntityManager.persist(Token.builder()
				.token(VALID_TOKEN)
				.tokenType(TokenType.BEARER)
				.user(user)
				.expired(false)
				.revoked(false)
				.build());

		testEntityManager.persist(Token.builder()
				.token(REVOKED_TOKEN)
				.tokenType(TokenType.BEARER)
				.user(user)
				.expired(true)
				.revoked(true)
				.build());
	}

	@Test
	@DisplayName("Find by token case found")
	public void findByTokenCaseFound() {
		Optional<Token> tokenDb = tokenRepository.findByToken(VALID_TOKEN);

		assertTrue(tokenDb.isPresent());
		assertThat(tokenDb.get().getToken()).isEqualTo(VALID_TOKEN);
	}

	@Test
	@DisplayName("Find by token case not found")
	public void findByTokenCaseNotFound() {
		Optional<Token> tokenDb = tokenRepository.findByToken("unknown-token");

		assertTrue(tokenDb.isEmpty());
	}

	@Test
	@DisplayName("Find all valid token by user case found only valid token")
	public void findAllValidTokenByUserCaseFoundOnlyValidToken() {
		List<Token> tokens = tokenRepository.findAllValidTokenByUser(user.getId());

		assertThat(tokens).hasSize(1);
		assertThat(tokens.get(0).getToken()).isEqualTo(VALID_TOKEN);
	}

	@Test
	@DisplayName("Find all valid token by user case user without tokens")
	public void findAllValidTokenByUserCaseUserWithoutTokens() {
		SecurityUser otherUser = testEntityManager.persist(SecurityUser.builder()
				.username("mariaCasas")
				.password("1234")
				.firstname("maria")
				.lastname("casas")
				.email("maria@casas.com")
				.role(Role.USER)
				.build());

		List<Token> tokens = tokenRepository.findAllValidTokenByUser(otherUser.getId());

		assertThat(tokens).isEmpty();
	}

	@Test
	@DisplayName("Save token case success")
	public void saveTokenCaseSuccess() {
		Token newToken = Token.builder()
				.token("new-token")
				.tokenType(TokenType.BEARER)
				.user(user)
				.expired(false)
				.revoked(false)
				.build();

		Token savedToken = tokenRepository.save(newToken);

		assertThat(savedToken).isNotNull();
		assertThat(savedToken.getId()).isNotNull();
	}
}
