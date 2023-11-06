package com.app.core.security.entity;

import static com.app.core.security.entity.Permission.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
	ANONYMOUS(Collections.emptySet()),
	USER(Collections.emptySet()),
	ADMIN(
			Set.of(
					ADMIN_READ,
	                  ADMIN_UPDATE,
	                  ADMIN_DELETE,
	                  ADMIN_CREATE,
	                  MANAGER_READ,
	                  MANAGER_UPDATE,
	                  MANAGER_DELETE,
	                  MANAGER_CREATE
					)
			),
	MANAGER(
	          Set.of(
	                  MANAGER_READ,
	                  MANAGER_UPDATE,
	                  MANAGER_DELETE,
	                  MANAGER_CREATE
	          )
	  );
	
	@Getter
	  private final Set<Permission> permissions;
	
	public List<SimpleGrantedAuthority> getAuthorities() {
	    var authorities = getPermissions()
	            .stream()
	            .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
	            .collect(Collectors.toList());
	    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
	    return authorities;
	  }
}
