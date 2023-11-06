package com.app.core.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.core.entity.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, UUID>{
	Optional<UserModel> findByFirstname(final String name);
	Optional<UserModel> findByUsername(final String username);
}
