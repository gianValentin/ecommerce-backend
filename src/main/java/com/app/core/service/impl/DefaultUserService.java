package com.app.core.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.app.core.entity.model.UserModel;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.UserRepository;
import com.app.core.security.entity.Role;
import com.app.core.service.UserService;
import com.app.core.utils.CustomCodeException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DefaultUserService implements UserService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserModel getByName(String name) {
		Assert.notNull(name, "name cannot be null");
		return userRepository.findByFirstname(name).orElseThrow(
				() -> new CJNotFoundException(CustomCodeException.CODE_404, "user not found with name "+name));
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserModel> getAll() {
		return userRepository.findAll();
	}

	@Override
	@Transactional
	public UserModel save(UserModel user) {
		Assert.notNull(user, "user cannot be null");
		return userRepository.save(user);
	}

	@Override
	@Transactional
	public UserModel update(UserModel user, UUID id) {

		Assert.notNull(id, "id cannot be null");
		Assert.notNull(user, "user cannot be null");

		UserModel userDb = userRepository.findById(id)
				.orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_400, "user not found"));

		userDb.setUsername(user.getUsername());
		userDb.setFirstname(user.getFirstname());
		userDb.setLastname(user.getLastname());
		userDb.setPassword(user.getPassword());
		userDb.setEmail(user.getEmail());		

		return userRepository.save(userDb);
	}

	@Override
	@Transactional
	public void deleteById(UUID id) {
		Assert.notNull(id, "id cannot be null");
		UserModel userDb = userRepository.findById(id)
				.orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_400, "user not found"));
		userRepository.delete(userDb);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserModel> finadAll(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	@Override
	public UserModel getSessionUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}		
		
		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404 , "user not found"));
	}

	@Override
	public boolean isAnonymousSession() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication instanceof AnonymousAuthenticationToken) {
			return true;
		}
		
		return false;
	}

	@Override
	public UserModel generateUserAnonymous() {
		UserModel user = UserModel.builder()
				.username("anonymous")
				.role(Role.ANONYMOUS)
				.build();
		return userRepository.save(user);
	}

}
