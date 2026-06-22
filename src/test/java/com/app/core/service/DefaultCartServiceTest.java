package com.app.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.app.core.entity.model.CType;
import com.app.core.entity.model.CartModel;
import com.app.core.entity.model.EntryModel;
import com.app.core.entity.model.PriceModel;
import com.app.core.entity.model.ProductModel;
import com.app.core.entity.model.UserModel;
import com.app.core.exception.CECartException;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.CartRepository;
import com.app.core.security.entity.Role;
import com.app.core.service.impl.DefaultCartService;

public class DefaultCartServiceTest {

	private static final UUID CART_ID = UUID.randomUUID();
	private static final UUID USER_ID = UUID.randomUUID();

	private CartRepository cartRepository;
	private UserService userService;
	private ProductService productService;
	private DefaultCartService cartService;

	private UserModel registeredUser;

	@BeforeEach
	void setUp() {
		cartRepository = mock(CartRepository.class);
		userService = mock(UserService.class);
		productService = mock(ProductService.class);
		cartService = new DefaultCartService(cartRepository, userService, productService);

		registeredUser = UserModel.builder().id(USER_ID).username("gianValentin").role(Role.USER).build();
	}

	@Test
	@DisplayName("Get cart case registered user without an existing cart should create a new one")
	public void getCartCaseRegisteredUserWithoutExistingCartShouldCreateNewOne() {
		CartModel newCart = CartModel.builder().id(CART_ID).user(registeredUser).build();

		Mockito.when(userService.isAnonymousSession()).thenReturn(false);
		Mockito.when(userService.getSessionUser()).thenReturn(registeredUser);
		Mockito.when(cartRepository.findCartAvailableByUser(USER_ID, CType.CART)).thenReturn(List.of());
		Mockito.when(cartRepository.findCartByIdAndUserRole(isNull(), eq(CType.CART), eq(Role.ANONYMOUS))).thenReturn(List.of());
		Mockito.when(cartRepository.save(any(CartModel.class))).thenReturn(newCart);

		CartModel result = cartService.getCart();

		assertNotNull(result);
		verify(cartRepository).save(any(CartModel.class));
	}

	@Test
	@DisplayName("Get cart case registered user with an existing cart should reuse it")
	public void getCartCaseRegisteredUserWithExistingCartShouldReuseIt() {
		CartModel existingCart = CartModel.builder().id(CART_ID).user(registeredUser).build();

		Mockito.when(userService.isAnonymousSession()).thenReturn(false);
		Mockito.when(userService.getSessionUser()).thenReturn(registeredUser);
		Mockito.when(cartRepository.findCartAvailableByUser(USER_ID, CType.CART)).thenReturn(List.of(existingCart));
		Mockito.when(cartRepository.findCartByIdAndUserRole(isNull(), eq(CType.CART), eq(Role.ANONYMOUS))).thenReturn(List.of());

		CartModel result = cartService.getCart();

		assertEquals(CART_ID, result.getId());
		verify(cartRepository, never()).save(any(CartModel.class));
	}

	@Test
	@DisplayName("Get cart case registered user merges anonymous cart with session cart")
	public void getCartCaseRegisteredUserMergesAnonymousCartWithSessionCart() {
		String anonymousCartId = UUID.randomUUID().toString();
		CartModel sessionCart = CartModel.builder().id(CART_ID).user(registeredUser).build();
		UserModel anonymousUser = UserModel.builder().id(UUID.randomUUID()).role(Role.ANONYMOUS).build();
		CartModel anonymousCart = CartModel.builder().id(UUID.fromString(anonymousCartId)).user(anonymousUser).build();
		anonymousCart.addEntry(EntryModel.builder().id(1L).build());

		Mockito.when(userService.isAnonymousSession()).thenReturn(false);
		Mockito.when(userService.getSessionUser()).thenReturn(registeredUser);
		Mockito.when(cartRepository.findCartAvailableByUser(USER_ID, CType.CART)).thenReturn(List.of(sessionCart));
		Mockito.when(cartRepository.findCartByIdAndUserRole(UUID.fromString(anonymousCartId), CType.CART, Role.ANONYMOUS))
				.thenReturn(List.of(anonymousCart));
		Mockito.when(cartRepository.save(sessionCart)).thenReturn(sessionCart);

		CartModel result = cartService.getCart(anonymousCartId);

		assertEquals(1, result.getEntries().size());
		verify(cartRepository).delete(anonymousCart);
		verify(cartRepository).save(sessionCart);
	}

	@Test
	@DisplayName("Get cart case anonymous user with matching cart should return it")
	public void getCartCaseAnonymousUserWithMatchingCartShouldReturnIt() {
		String anonymousCartId = UUID.randomUUID().toString();
		UserModel anonymousUser = UserModel.builder().id(UUID.randomUUID()).role(Role.ANONYMOUS).build();
		CartModel anonymousCart = CartModel.builder().id(UUID.fromString(anonymousCartId)).user(anonymousUser).build();

		Mockito.when(userService.isAnonymousSession()).thenReturn(true);
		Mockito.when(cartRepository.findCartByIdAndUserRole(UUID.fromString(anonymousCartId), CType.CART, Role.ANONYMOUS))
				.thenReturn(List.of(anonymousCart));

		CartModel result = cartService.getCart(anonymousCartId);

		assertEquals(anonymousCart.getId(), result.getId());
		verify(cartRepository, never()).save(any(CartModel.class));
	}

	@Test
	@DisplayName("Get cart case anonymous user without an existing cart should create a new one")
	public void getCartCaseAnonymousUserWithoutExistingCartShouldCreateNewOne() {
		UserModel anonymousUser = UserModel.builder().id(UUID.randomUUID()).role(Role.ANONYMOUS).build();
		CartModel newAnonymousCart = CartModel.builder().id(CART_ID).user(anonymousUser).build();

		Mockito.when(userService.isAnonymousSession()).thenReturn(true);
		Mockito.when(cartRepository.findCartByIdAndUserRole(isNull(), eq(CType.CART), eq(Role.ANONYMOUS))).thenReturn(List.of());
		Mockito.when(userService.generateUserAnonymous()).thenReturn(anonymousUser);
		Mockito.when(cartRepository.save(any(CartModel.class))).thenReturn(newAnonymousCart);

		CartModel result = cartService.getCart();

		assertNotNull(result);
		verify(cartRepository).save(any(CartModel.class));
	}

	@Test
	@DisplayName("Get cart case invalid uuid format is treated as null")
	public void getCartCaseInvalidUuidFormatIsTreatedAsNull() {
		Mockito.when(userService.isAnonymousSession()).thenReturn(true);
		Mockito.when(cartRepository.findCartByIdAndUserRole(isNull(), eq(CType.CART), eq(Role.ANONYMOUS))).thenReturn(List.of());
		Mockito.when(userService.generateUserAnonymous()).thenReturn(UserModel.builder().role(Role.ANONYMOUS).build());
		Mockito.when(cartRepository.save(any(CartModel.class)))
				.thenReturn(CartModel.builder().id(CART_ID).build());

		CartModel result = cartService.getCart("not-a-valid-uuid");

		assertNotNull(result);
		verify(cartRepository).findCartByIdAndUserRole(isNull(), eq(CType.CART), eq(Role.ANONYMOUS));
	}

	@Test
	@DisplayName("Exists cart by id case invalid format returns false")
	public void existsCartByIdCaseInvalidFormatReturnsFalse() {
		assertFalse(cartService.existsCartById("not-a-valid-uuid"));
	}

	@Test
	@DisplayName("Exists cart by id case anonymous session without matching cart returns false")
	public void existsCartByIdCaseAnonymousSessionWithoutMatchingCartReturnsFalse() {
		Mockito.when(cartRepository.findCartByIdAndUserRole(CART_ID, CType.CART, Role.ANONYMOUS)).thenReturn(List.of());
		Mockito.when(userService.isAnonymousSession()).thenReturn(true);

		assertFalse(cartService.existsCartById(CART_ID.toString()));
	}

	@Test
	@DisplayName("Exists cart by id case registered session without matching anonymous cart returns true")
	public void existsCartByIdCaseRegisteredSessionWithoutMatchingAnonymousCartReturnsTrue() {
		Mockito.when(cartRepository.findCartByIdAndUserRole(CART_ID, CType.CART, Role.ANONYMOUS)).thenReturn(List.of());
		Mockito.when(userService.isAnonymousSession()).thenReturn(false);

		assertTrue(cartService.existsCartById(CART_ID.toString()));
	}

	@Test
	@DisplayName("Validate if exists cart by id case not found should throw exception")
	public void validateIfExistsCartByIdCaseNotFoundShouldThrowException() {
		assertThrows(CJNotFoundException.class, () -> cartService.validateIfExistsCartById("not-a-valid-uuid"));
	}

	@Test
	@DisplayName("Calculate cart case empty entries sets totals to zero")
	public void calculateCartCaseEmptyEntriesSetsTotalsToZero() {
		CartModel cart = CartModel.builder().build();

		cartService.calculateCart(cart);

		assertEquals(0.0, cart.getSubTotal());
		assertEquals(0.0, cart.getDiscount());
		assertEquals(0.0, cart.getTotal());
	}

	@Test
	@DisplayName("Calculate cart case entries with price calculates totals")
	public void calculateCartCaseEntriesWithPriceCalculatesTotals() {
		ProductModel product = ProductModel.builder().price(PriceModel.builder().price(10.0).build()).build();
		EntryModel entry = EntryModel.builder().id(1L).amount(3).product(product).build();
		CartModel cart = CartModel.builder().build();
		cart.addEntry(entry);

		cartService.calculateCart(cart);

		assertEquals(30.0, cart.getSubTotal());
		assertEquals(30.0, cart.getTotal());
		assertEquals(30.0, entry.getTotal());
	}

	@Test
	@DisplayName("Calculate cart case entry without product is ignored")
	public void calculateCartCaseEntryWithoutProductIsIgnored() {
		EntryModel entry = EntryModel.builder().id(1L).amount(3).build();
		CartModel cart = CartModel.builder().build();
		cart.addEntry(entry);

		cartService.calculateCart(cart);

		assertEquals(0.0, cart.getSubTotal());
	}

	@Test
	@DisplayName("Add to card case success")
	public void addToCardCaseSuccess() {
		String code = "PR001";
		ProductModel product = ProductModel.builder().code(code).price(PriceModel.builder().price(10.0).build()).build();
		CartModel cart = CartModel.builder().id(CART_ID).build();

		Mockito.when(productService.getProductByCode(code)).thenReturn(product);
		Mockito.when(cartRepository.save(cart)).thenReturn(cart);

		CartModel result = cartService.addToCard(cart, code, 2);

		assertEquals(1, result.getEntries().size());
		assertEquals(20.0, result.getSubTotal());
	}

	@Test
	@DisplayName("Add to card case product without price should throw exception")
	public void addToCardCaseProductWithoutPriceShouldThrowException() {
		String code = "PR001";
		ProductModel product = ProductModel.builder().code(code).build();
		CartModel cart = CartModel.builder().id(CART_ID).build();

		Mockito.when(productService.getProductByCode(code)).thenReturn(product);

		assertThrows(CECartException.class, () -> cartService.addToCard(cart, code, 2));
	}

	@Test
	@DisplayName("Merge cart case moves entries from old cart and deletes it")
	public void mergeCartCaseMovesEntriesFromOldCartAndDeletesIt() {
		CartModel currentCart = CartModel.builder().id(CART_ID).build();
		CartModel oldCart = CartModel.builder().id(UUID.randomUUID()).build();
		oldCart.addEntry(EntryModel.builder().id(1L).build());

		cartService.mergeCart(currentCart, oldCart);

		assertEquals(1, currentCart.getEntries().size());
		verify(cartRepository).delete(oldCart);
	}

	@Test
	@DisplayName("Remove entry case empty cart should throw exception")
	public void removeEntryCaseEmptyCartShouldThrowException() {
		CartModel cart = CartModel.builder().id(CART_ID).build();

		assertThrows(CECartException.class, () -> cartService.removeEntry(cart, 1L));
	}

	@Test
	@DisplayName("Remove entry case entry not found should throw exception")
	public void removeEntryCaseEntryNotFoundShouldThrowException() {
		CartModel cart = CartModel.builder().id(CART_ID).build();
		cart.addEntry(EntryModel.builder().id(1L).build());

		assertThrows(CJNotFoundException.class, () -> cartService.removeEntry(cart, 2L));
	}

	@Test
	@DisplayName("Remove entry case success should remove the entry and recalculate")
	public void removeEntryCaseSuccessShouldRemoveEntryAndRecalculate() {
		CartModel cart = CartModel.builder().id(CART_ID).build();
		cart.addEntry(EntryModel.builder().id(1L).build());

		Mockito.when(cartRepository.save(cart)).thenReturn(cart);

		CartModel result = cartService.removeEntry(cart, 1L);

		assertTrue(result.getEntries().isEmpty());
	}
}
