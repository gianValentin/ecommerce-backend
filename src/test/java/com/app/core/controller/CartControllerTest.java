package com.app.core.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.app.core.entity.dto.cart.RequestAddToCartDTO;
import com.app.core.entity.dto.cart.RequestRemoveEntryCartDTO;
import com.app.core.entity.dto.cart.ResponseCartDTO;
import com.app.core.entity.model.CartModel;
import com.app.core.security.JwtAuthenticationFilter;
import com.app.core.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
		controllers = CartController.class,
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = JwtAuthenticationFilter.class))
@Import(ValidationConfig.class)
public class CartControllerTest {

	private final String path = "/api/v1/cart";
	private final UUID cartId = UUID.randomUUID();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CartService cartService;

	@MockBean
	private ModelMapper modelMapper;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@WithMockUser
	@DisplayName("Get cart case found")
	public void getCartCaseFound() throws Exception {
		CartModel cart = CartModel.builder().id(cartId).build();
		ResponseCartDTO dto = new ResponseCartDTO();
		dto.setId(cartId);

		Mockito.when(cartService.getCart(cartId.toString())).thenReturn(cart);
		Mockito.when(modelMapper.map(cart, ResponseCartDTO.class)).thenReturn(dto);

		mockMvc.perform(get(path.concat("/{id}"), cartId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(cartId.toString()));
	}

	@Test
	@WithMockUser
	@DisplayName("Get cart case not found")
	public void getCartCaseNotFound() throws Exception {
		Mockito.when(cartService.getCart(cartId.toString())).thenReturn(null);

		mockMvc.perform(get(path.concat("/{id}"), cartId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	@DisplayName("Add to cart case success")
	public void addToCartCaseSuccess() throws Exception {
		CartModel cart = CartModel.builder().id(cartId).build();
		CartModel cartModified = CartModel.builder().id(cartId).build();
		ResponseCartDTO dto = new ResponseCartDTO();
		dto.setId(cartId);
		RequestAddToCartDTO request = new RequestAddToCartDTO(2, "PR001");

		Mockito.when(cartService.getCart(cartId.toString())).thenReturn(cart);
		Mockito.when(cartService.addToCard(cart, "PR001", 2)).thenReturn(cartModified);
		Mockito.when(modelMapper.map(cartModified, ResponseCartDTO.class)).thenReturn(dto);

		mockMvc.perform(post(path.concat("/{id}/addToCart"), cartId)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(cartId.toString()));
	}

	@Test
	@WithMockUser
	@DisplayName("Remove entry case success")
	public void removeEntryCaseSuccess() throws Exception {
		CartModel cart = CartModel.builder().id(cartId).build();
		CartModel cartModified = CartModel.builder().id(cartId).build();
		ResponseCartDTO dto = new ResponseCartDTO();
		dto.setId(cartId);
		RequestRemoveEntryCartDTO request = new RequestRemoveEntryCartDTO(1L);

		Mockito.when(cartService.getCart(cartId.toString())).thenReturn(cart);
		Mockito.when(cartService.removeEntry(cart, 1L)).thenReturn(cartModified);
		Mockito.when(modelMapper.map(cartModified, ResponseCartDTO.class)).thenReturn(dto);

		mockMvc.perform(delete(path.concat("/{id}/entry"), cartId)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(cartId.toString()));

		Mockito.verify(cartService).validateIfExistsCartById(cartId.toString());
	}
}
