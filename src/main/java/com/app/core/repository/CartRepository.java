package com.app.core.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.core.entity.model.CType;
import com.app.core.entity.model.CartModel;
import com.app.core.security.entity.Role;

public interface CartRepository extends JpaRepository<CartModel, UUID>{
	@Query(value = """
			select c from CartModel c inner join UserModel  u\s
			on c.user.id = u.id\s
			where u.id = :id and c.type =:type\s
			""")
	List<CartModel> findCartAvailableByUser(UUID id, CType type);
	
	@Query(value = """
			select c from CartModel c inner join UserModel  u\s
			on c.user.id = u.id\s
			where c.id = :id and c.type =:type and u.role = :role\s
			""")
	List<CartModel>findCartByIdAndUserRole(UUID id, CType type, Role role);
	
	@Query(value = """
			select c from CartModel c inner join UserModel  u\s
			on c.user.id = u.id\s
			where u.id = :id and c.type =:type and u.role = :role\s
			""")
	Page<CartModel>findCartsByUserAndCTypeAndUserRole(UUID id, CType type, Role role,Pageable pageable);
	
	@Query(value = """
			select c from CartModel c inner join UserModel  u\s
			on c.user.id = u.id\s
			where u.id = :id and c.type =:type and u.role = :role\s
			""")
	List<CartModel>findCartsByUserAndCTypeAndUserRole(UUID id, CType type, Role role);
	
	@Query(value = """
			select c from CartModel c inner join UserModel  u\s
			on c.user.id = u.id\s
			where c.id = :cartId and u.id = :userId and c.type =:type and u.role = :role\s
			""")
	List<CartModel> findByCartIdAndUserIdAndCTypeAndUserRole(UUID cartId, UUID userId, CType type, Role role);
}
