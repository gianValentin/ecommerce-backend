package com.app.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.core.entity.model.UserModel;

public interface UserService {	
	UserModel getByName(final String name);
	List<UserModel> getAll();
	UserModel save(final UserModel user);
	UserModel update(final UserModel user, final UUID id);
	void deleteById(final UUID id);
	Page<UserModel> finadAll(Pageable pageable);
	UserModel getSessionUser();
	boolean isAnonymousSession();
	UserModel generateUserAnonymous();
}
