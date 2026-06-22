package com.app.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.app.core.entity.model.CType;
import com.app.core.entity.model.CartModel;
import com.app.core.entity.model.UserModel;
import com.app.core.security.entity.Role;

/**
 * Usa el datasource definido por el perfil Maven activo (h2 o postgresql),
 * tal como lo hace la aplicacion en runtime. Ejecutar con:
 * ./mvnw test -Ph2 (default) o ./mvnw test -Ppostgresql
 */
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CartRepositoryTest {

	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private TestEntityManager testEntityManager;

	private UserModel registeredUser;
	private UserModel anonymousUser;
	private CartModel cartOfRegisteredUser;
	private CartModel cartOfAnonymousUser;

	@BeforeEach
	void setUp() {
		cartRepository.deleteAll();

		registeredUser = testEntityManager.persist(UserModel.builder()
				.username("gianValentin")
				.firstname("giancarlo")
				.password("1234")
				.email("giancarlo@valentin.com")
				.role(Role.USER)
				.build());

		anonymousUser = testEntityManager.persist(UserModel.builder()
				.username("anonymous-" + UUID.randomUUID())
				.role(Role.ANONYMOUS)
				.build());

		cartOfRegisteredUser = testEntityManager.persist(CartModel.builder()
				.user(registeredUser)
				.type(CType.CART)
				.build());

		cartOfAnonymousUser = testEntityManager.persist(CartModel.builder()
				.user(anonymousUser)
				.type(CType.CART)
				.build());
	}

	@Test
	@DisplayName("Find cart available by user case found")
	public void findCartAvailableByUserCaseFound() {
		List<CartModel> carts = cartRepository.findCartAvailableByUser(registeredUser.getId(), CType.CART);

		assertThat(carts).hasSize(1);
		assertThat(carts.get(0).getId()).isEqualTo(cartOfRegisteredUser.getId());
	}

	@Test
	@DisplayName("Find cart available by user case not found")
	public void findCartAvailableByUserCaseNotFound() {
		List<CartModel> carts = cartRepository.findCartAvailableByUser(UUID.randomUUID(), CType.CART);

		assertThat(carts).isEmpty();
	}

	@Test
	@DisplayName("Find cart by id and user role case found")
	public void findCartByIdAndUserRoleCaseFound() {
		List<CartModel> carts = cartRepository.findCartByIdAndUserRole(cartOfAnonymousUser.getId(), CType.CART, Role.ANONYMOUS);

		assertThat(carts).hasSize(1);
		assertThat(carts.get(0).getId()).isEqualTo(cartOfAnonymousUser.getId());
	}

	@Test
	@DisplayName("Find cart by id and user role case role does not match")
	public void findCartByIdAndUserRoleCaseRoleDoesNotMatch() {
		List<CartModel> carts = cartRepository.findCartByIdAndUserRole(cartOfAnonymousUser.getId(), CType.CART, Role.USER);

		assertThat(carts).isEmpty();
	}

	@Test
	@DisplayName("Find carts by user, type and role case found as list")
	public void findCartsByUserAndCTypeAndUserRoleCaseFoundAsList() {
		List<CartModel> carts = cartRepository.findCartsByUserAndCTypeAndUserRole(registeredUser.getId(), CType.CART, Role.USER);

		assertThat(carts).hasSize(1);
	}

	@Test
	@DisplayName("Find carts by user, type and role case found as page")
	public void findCartsByUserAndCTypeAndUserRoleCaseFoundAsPage() {
		Page<CartModel> carts = cartRepository.findCartsByUserAndCTypeAndUserRole(registeredUser.getId(), CType.CART, Role.USER,
				PageRequest.of(0, 10));

		assertThat(carts.getTotalElements()).isEqualTo(1);
	}

	@Test
	@DisplayName("Find by cart id, user id, type and role case found")
	public void findByCartIdAndUserIdAndCTypeAndUserRoleCaseFound() {
		List<CartModel> carts = cartRepository.findByCartIdAndUserIdAndCTypeAndUserRole(
				cartOfRegisteredUser.getId(), registeredUser.getId(), CType.CART, Role.USER);

		assertThat(carts).hasSize(1);
	}

	@Test
	@DisplayName("Find by cart id, user id, type and role case not found")
	public void findByCartIdAndUserIdAndCTypeAndUserRoleCaseNotFound() {
		List<CartModel> carts = cartRepository.findByCartIdAndUserIdAndCTypeAndUserRole(
				cartOfRegisteredUser.getId(), anonymousUser.getId(), CType.CART, Role.USER);

		assertThat(carts).isEmpty();
	}

	@Test
	@DisplayName("Save cart case success")
	public void saveCartCaseSuccess() {
		CartModel newCart = CartModel.builder().user(registeredUser).build();

		CartModel savedCart = cartRepository.save(newCart);

		assertThat(savedCart).isNotNull();
		assertThat(savedCart.getId()).isNotNull();
	}

	@Test
	@DisplayName("Delete cart case success")
	public void deleteCartCaseSuccess() {
		cartRepository.deleteById(cartOfRegisteredUser.getId());

		assertThat(cartRepository.findById(cartOfRegisteredUser.getId())).isEmpty();
	}
}
