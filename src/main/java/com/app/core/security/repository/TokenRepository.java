package com.app.core.security.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.app.core.security.entity.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
	@Query(value = """
			select t from Token t inner join SecurityUser u\s
			on t.user.id = u.id\s
			where u.id = :id and t.expired = false and t.revoked = false\s
			""")
	List<Token> findAllValidTokenByUser(UUID id);

	Optional<Token> findByToken(String token);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from Token t where t.token = :token and t.tokenType = com.app.core.security.entity.TokenType.REFRESH and t.expired = false and t.revoked = false")
	Optional<Token> findActiveRefreshTokenForUpdate(String token);
}
