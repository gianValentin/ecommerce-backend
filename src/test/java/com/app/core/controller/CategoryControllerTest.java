package com.app.core.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.app.core.entity.dto.category.ResponseCategoryDTO;
import com.app.core.entity.dto.category.SaveCategoryDTO;
import com.app.core.entity.dto.category.UpdateCategoryDTO;
import com.app.core.security.JwtAuthenticationFilter;
import com.app.core.service.CategoryService;
import com.app.core.utils.CustomCodeException;
import com.app.core.exception.CJNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
		controllers = CategoryController.class,
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = JwtAuthenticationFilter.class))
@Import({ValidationConfig.class, ModelMapperConfig.class})
public class CategoryControllerTest {

	private final String path = "/api/v1/categories";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CategoryService categoryService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@WithMockUser
	@DisplayName("Get all case found all categories")
	public void getAllCaseFoundAllCategories() throws Exception {
		ResponseCategoryDTO category = new ResponseCategoryDTO();
		category.setId(1L);
		category.setName("electronics");

		Mockito.when(categoryService.getAll()).thenReturn(List.of(category));

		mockMvc.perform(get(path).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("electronics"));
	}

	@Test
	@WithMockUser
	@DisplayName("Get by id case found")
	public void getByIdCaseFound() throws Exception {
		ResponseCategoryDTO category = new ResponseCategoryDTO();
		category.setId(1L);
		category.setName("electronics");

		Mockito.when(categoryService.findById(1L)).thenReturn(category);

		mockMvc.perform(get(path.concat("/{id}"), 1L).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value("electronics"));
	}

	@Test
	@WithMockUser
	@DisplayName("Get by id case not found")
	public void getByIdCaseNotFound() throws Exception {
		Mockito.when(categoryService.findById(1L))
				.thenThrow(new CJNotFoundException(CustomCodeException.CODE_404, "Category with id [1] not Found"));

		mockMvc.perform(get(path.concat("/{id}"), 1L).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$.code").value(CustomCodeException.CODE_404));
	}

	@Test
	@WithMockUser
	@DisplayName("Save case success")
	public void saveCaseSuccess() throws Exception {
		SaveCategoryDTO saveCategoryDTO = new SaveCategoryDTO("furniture");
		ResponseCategoryDTO responseCategoryDTO = new ResponseCategoryDTO();
		responseCategoryDTO.setId(1L);
		responseCategoryDTO.setName("furniture");

		Mockito.when(categoryService.save(Mockito.any(SaveCategoryDTO.class))).thenReturn(responseCategoryDTO);

		mockMvc.perform(post(path)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(saveCategoryDTO)))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value("furniture"));
	}

	@Test
	@WithMockUser
	@DisplayName("Update case success")
	public void updateCaseSuccess() throws Exception {
		UpdateCategoryDTO updateCategoryDTO = new UpdateCategoryDTO("clothing");
		ResponseCategoryDTO responseCategoryDTO = new ResponseCategoryDTO();
		responseCategoryDTO.setId(1L);
		responseCategoryDTO.setName("clothing");

		Mockito.when(categoryService.update(Mockito.any(UpdateCategoryDTO.class), Mockito.eq(1L)))
				.thenReturn(responseCategoryDTO);

		mockMvc.perform(put(path.concat("/{id}"), 1L)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateCategoryDTO)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value("clothing"));
	}

	@Test
	@WithMockUser
	@DisplayName("Delete case success")
	public void deleteCaseSuccess() throws Exception {
		mockMvc.perform(delete(path.concat("/{id}"), 1L).with(csrf()))
				.andExpect(status().isNoContent());

		Mockito.verify(categoryService).deleteById(1L);
	}
}
