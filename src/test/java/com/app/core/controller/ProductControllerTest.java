package com.app.core.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import com.app.core.config.ModelMapperConfig;
import com.app.core.config.ValidationConfig;
import com.app.core.entity.dto.category.CategoryDTO;
import com.app.core.entity.dto.image.ImageDTO;
import com.app.core.entity.dto.price.PriceDTO;
import com.app.core.entity.dto.product.ResponseProductDTO;
import com.app.core.entity.dto.product.SaveProductDTO;
import com.app.core.entity.dto.product.UpdateProductDTO;
import com.app.core.exception.CJNotFoundException;
import com.app.core.security.JwtAuthenticationFilter;
import com.app.core.service.ProductService;
import com.app.core.utils.CustomCodeException;
import com.app.core.validation.ProductCodeExistsConstraintValidation;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
		controllers = ProductController.class,
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = JwtAuthenticationFilter.class))
@Import({ValidationConfig.class, ModelMapperConfig.class, ProductCodeExistsConstraintValidation.class})
public class ProductControllerTest {

	private final String path = "/api/v1/product";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService productService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private ResponseProductDTO buildResponseProductDTO() {
		ResponseProductDTO dto = new ResponseProductDTO();
		dto.setId(1L);
		dto.setCode("PR001");
		dto.setName("Laptop");
		return dto;
	}

	private SaveProductDTO buildSaveProductDTO() {
		SaveProductDTO dto = new SaveProductDTO();
		dto.setName("Laptop");
		dto.setCode("PR001");
		dto.setCategory(new CategoryDTO(1L, "electronics"));
		dto.setPrice(new PriceDTO(null, java.math.BigDecimal.valueOf(999)));
		dto.setImages(Set.of(new ImageDTO(null, "http://image.com/1.png")));
		return dto;
	}

	@Test
	@WithMockUser
	@DisplayName("Get all case found all products")
	public void getAllCaseFoundAllProducts() throws Exception {
		Mockito.when(productService.getAll((Long) null)).thenReturn(List.of(buildResponseProductDTO()));

		mockMvc.perform(get(path).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].code").value("PR001"));
	}

	@Test
	@WithMockUser
	@DisplayName("Get by id case found")
	public void getByIdCaseFound() throws Exception {
		Mockito.when(productService.findById(1L)).thenReturn(buildResponseProductDTO());

		mockMvc.perform(get(path.concat("/{id}"), 1L).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PR001"));
	}

	@Test
	@WithMockUser
	@DisplayName("Get by id case not found")
	public void getByIdCaseNotFound() throws Exception {
		Mockito.when(productService.findById(1L))
				.thenThrow(new CJNotFoundException(CustomCodeException.CODE_404, "Product with id [1] not Found"));

		mockMvc.perform(get(path.concat("/{id}"), 1L).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(CustomCodeException.CODE_404));
	}

	@Test
	@WithMockUser
	@DisplayName("Save case success")
	public void saveCaseSuccess() throws Exception {
		Mockito.when(productService.isCodeValid("PR001")).thenReturn(true);
		Mockito.when(productService.saveAll(Mockito.anyList())).thenReturn(List.of(buildResponseProductDTO()));

		mockMvc.perform(post(path)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(List.of(buildSaveProductDTO()))))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].code").value("PR001"));
	}

	@Test
	@WithMockUser
	@DisplayName("Save case invalid product code returns bad request")
	public void saveCaseInvalidProductCodeReturnsBadRequest() throws Exception {
		Mockito.when(productService.isCodeValid("PR001")).thenReturn(false);

		mockMvc.perform(post(path)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(List.of(buildSaveProductDTO()))))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	@DisplayName("Update case success")
	public void updateCaseSuccess() throws Exception {
		UpdateProductDTO updateProductDTO = new UpdateProductDTO();
		updateProductDTO.setName("Laptop Pro");

		Mockito.when(productService.update(Mockito.any(UpdateProductDTO.class), Mockito.eq(1L)))
				.thenReturn(buildResponseProductDTO());

		mockMvc.perform(put(path.concat("/{id}"), 1L)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateProductDTO)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.code").value("PR001"));
	}

	@Test
	@WithMockUser
	@DisplayName("Delete case success")
	public void deleteCaseSuccess() throws Exception {
		mockMvc.perform(delete(path.concat("/{id}"), 1L).with(csrf()))
				.andExpect(status().isNoContent());

		Mockito.verify(productService).deleteById(1L);
	}
}
