package com.app.core.service;

import static org.mockito.Mockito.mock;

import java.util.UUID;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.app.core.entity.dto.cart.ResponseCartDTO;
import com.app.core.entity.model.CartModel;
import com.app.core.entity.model.UserModel;
import com.app.core.service.impl.DefaultNotificationService;

@SpringBootTest
public class DefaultNotificationServiceTest {

	@Autowired
	private NotificationService notificationService;

	@MockBean
	private SimpMessagingTemplate messagingTemplate;

	@MockBean
	private ModelMapper modelMapper;

	@Test
	@DisplayName("Should send WebSocket notification to the user when order is generated")
	public void notifyOrderGeneratedShouldSendToUser() {
		UserModel user = UserModel.builder()
				.id(UUID.randomUUID())
				.username("testuser")
				.build();
		CartModel order = CartModel.builder()
				.id(UUID.randomUUID())
				.user(user)
				.build();
		ResponseCartDTO dto = new ResponseCartDTO();

		Mockito.when(modelMapper.map(order, ResponseCartDTO.class)).thenReturn(dto);

		notificationService.notifyOrderGenerated(order);

		Mockito.verify(messagingTemplate)
				.convertAndSendToUser("testuser", "/queue/orders", dto);
	}

	@Test
	@DisplayName("Should map CartModel to ResponseCartDTO before sending notification")
	public void notifyOrderGeneratedShouldMapOrderToDto() {
		UserModel user = UserModel.builder()
				.id(UUID.randomUUID())
				.username("testuser")
				.build();
		CartModel order = CartModel.builder()
				.id(UUID.randomUUID())
				.user(user)
				.build();
		ResponseCartDTO dto = new ResponseCartDTO();

		Mockito.when(modelMapper.map(order, ResponseCartDTO.class)).thenReturn(dto);

		notificationService.notifyOrderGenerated(order);

		Mockito.verify(modelMapper).map(order, ResponseCartDTO.class);
	}

	@Configuration
	@Import(DefaultNotificationService.class)
	static class TestConfig {
		@Bean
		SimpMessagingTemplate messagingTemplate() {
			return mock(SimpMessagingTemplate.class);
		}
		@Bean
		ModelMapper modelMapper() {
			return mock(ModelMapper.class);
		}
	}
}
