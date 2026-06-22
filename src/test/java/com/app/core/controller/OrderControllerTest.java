package com.app.core.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.app.core.config.ValidationConfig;
import com.app.core.entity.dto.cart.ResponseCartDTO;
import com.app.core.entity.model.CartModel;
import com.app.core.security.JwtAuthenticationFilter;
import com.app.core.service.OrderService;

@WebMvcTest(
		controllers = OrderController.class,
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = JwtAuthenticationFilter.class))
@Import(ValidationConfig.class)
public class OrderControllerTest {

	private final String path = "/api/v1/order";
	private final UUID orderId = UUID.randomUUID();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OrderService orderService;

	@MockBean
	private ModelMapper modelMapper;

	@Test
	@WithMockUser
	@DisplayName("Get all case found all orders")
	public void getAllCaseFoundAllOrders() throws Exception {
		ResponseCartDTO dto = new ResponseCartDTO();
		dto.setId(orderId);

		Mockito.when(orderService.getAll()).thenReturn(List.of(dto));

		mockMvc.perform(get(path).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(orderId.toString()));
	}

	@Test
	@WithMockUser
	@DisplayName("Get by id case found")
	public void getByIdCaseFound() throws Exception {
		ResponseCartDTO dto = new ResponseCartDTO();
		dto.setId(orderId);

		Mockito.when(orderService.getById(orderId.toString())).thenReturn(dto);

		mockMvc.perform(get(path.concat("/{orderId}"), orderId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(orderId.toString()));
	}

	@Test
	@WithMockUser
	@DisplayName("Place order case success")
	public void placeOrderCaseSuccess() throws Exception {
		CartModel order = CartModel.builder().id(orderId).build();
		ResponseCartDTO dto = new ResponseCartDTO();
		dto.setId(orderId);

		Mockito.when(orderService.placeOrder()).thenReturn(order);
		Mockito.when(modelMapper.map(order, ResponseCartDTO.class)).thenReturn(dto);

		mockMvc.perform(post(path.concat("/placeOrder")).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(orderId.toString()));
	}
}
