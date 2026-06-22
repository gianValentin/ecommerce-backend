package com.app.core.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.app.core.entity.model.CartModel;
import com.app.core.entity.model.CType;
import com.app.core.entity.model.EntryModel;
import com.app.core.entity.model.UserModel;
import com.app.core.exception.CECartException;
import com.app.core.repository.CartRepository;
import com.app.core.service.impl.DefaultOrderService;

@SpringBootTest
public class DefaultOrderServiceTest {

	@Autowired
	private OrderService orderService;

	@MockBean
	private CartRepository cartRepository;

	@MockBean
	private UserService userService;

	@MockBean
	private ModelMapper modelMapper;

	@MockBean
	private NotificationService notificationService;

	private UserModel userSession;
	private CartModel cartWithEntries;

	@BeforeEach
	void setUp() {
		userSession = UserModel.builder()
				.id(UUID.randomUUID())
				.username("testuser")
				.build();

		cartWithEntries = CartModel.builder()
				.id(UUID.randomUUID())
				.user(userSession)
				.build();
		cartWithEntries.addEntry(EntryModel.builder().id(1L).build());
	}

	@Test
	@DisplayName("PlaceOrder case anonymous session should throw exception")
	public void placeOrderCaseAnonymousSessionShouldThrowException() {
		Mockito.when(userService.isAnonymousSession()).thenReturn(true);

		assertThrows(CECartException.class, () -> orderService.placeOrder());
		Mockito.verify(notificationService, Mockito.never()).notifyOrderGenerated(any());
	}

	@Test
	@DisplayName("PlaceOrder case cart not found should throw exception")
	public void placeOrderCaseCartNotFoundShouldThrowException() {
		Mockito.when(userService.isAnonymousSession()).thenReturn(false);
		Mockito.when(userService.getSessionUser()).thenReturn(userSession);
		Mockito.when(cartRepository.findCartAvailableByUser(userSession.getId(), CType.CART))
				.thenReturn(List.of());

		assertThrows(CECartException.class, () -> orderService.placeOrder());
		Mockito.verify(notificationService, Mockito.never()).notifyOrderGenerated(any());
	}

	@Test
	@DisplayName("PlaceOrder case empty cart should throw exception")
	public void placeOrderCaseEmptyCartShouldThrowException() {
		CartModel emptyCart = CartModel.builder()
				.id(UUID.randomUUID())
				.user(userSession)
				.build();

		Mockito.when(userService.isAnonymousSession()).thenReturn(false);
		Mockito.when(userService.getSessionUser()).thenReturn(userSession);
		Mockito.when(cartRepository.findCartAvailableByUser(userSession.getId(), CType.CART))
				.thenReturn(List.of(emptyCart));

		assertThrows(CECartException.class, () -> orderService.placeOrder());
		Mockito.verify(notificationService, Mockito.never()).notifyOrderGenerated(any());
	}

	@Test
	@DisplayName("PlaceOrder case success should generate order and send WebSocket notification")
	public void placeOrderCaseSuccessShouldGenerateOrderAndSendNotification() {
		CartModel savedOrder = CartModel.builder()
				.id(cartWithEntries.getId())
				.user(userSession)
				.build();

		Mockito.when(userService.isAnonymousSession()).thenReturn(false);
		Mockito.when(userService.getSessionUser()).thenReturn(userSession);
		Mockito.when(cartRepository.findCartAvailableByUser(userSession.getId(), CType.CART))
				.thenReturn(List.of(cartWithEntries));
		Mockito.when(cartRepository.save(any(CartModel.class))).thenReturn(savedOrder);

		CartModel result = orderService.placeOrder();

		assertNotNull(result);
		Mockito.verify(cartRepository).save(cartWithEntries);
		Mockito.verify(notificationService).notifyOrderGenerated(savedOrder);
	}

	@Configuration
	@Import(DefaultOrderService.class)
	static class TestConfig {
		@Bean
		CartRepository cartRepository() { return mock(CartRepository.class); }
		@Bean
		UserService userService() { return mock(UserService.class); }
		@Bean
		ModelMapper modelMapper() { return mock(ModelMapper.class); }
		@Bean
		NotificationService notificationService() { return mock(NotificationService.class); }
	}
}
