package com.app.core.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.app.core.config.ModelMapperConfig;
import com.app.core.config.ValidationConfig;
import com.app.core.entity.dto.menu.ResponseMenuDTO;
import com.app.core.entity.dto.menu.SaveMenuDTO;
import com.app.core.entity.dto.menu.UpdateMenuDTO;
import com.app.core.security.JwtAuthenticationFilter;
import com.app.core.service.MenuService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
		controllers = MenuController.class,
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = JwtAuthenticationFilter.class))
@Import({ ValidationConfig.class, ModelMapperConfig.class })
class MenuControllerTest {

	private static final String PATH = "/api/v1/menus";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MenuService menuService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@WithMockUser
	void getAllMenus() throws Exception {
		ResponseMenuDTO response = menu("Resumen", "/home", "dashboard", 1, true);
		Mockito.when(menuService.getAll()).thenReturn(List.of(response));

		mockMvc.perform(get(PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Resumen"))
				.andExpect(jsonPath("$[0].path").value("/home"));
	}

	@Test
	@WithMockUser
	void getMenuById() throws Exception {
		Mockito.when(menuService.findById(1L)).thenReturn(menu("Resumen", "/home", "dashboard", 1, true));

		mockMvc.perform(get(PATH + "/{id}", 1L).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Resumen"));
	}

	@Test
	@WithMockUser
	void createMenu() throws Exception {
		SaveMenuDTO request = new SaveMenuDTO("Resumen", "/home", "dashboard", 1, true);
		Mockito.when(menuService.save(Mockito.any(SaveMenuDTO.class)))
				.thenReturn(menu("Resumen", "/home", "dashboard", 1, true));

		mockMvc.perform(post(PATH)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Resumen"));
	}

	@Test
	@WithMockUser
	void rejectMenuWithoutName() throws Exception {
		SaveMenuDTO request = new SaveMenuDTO(" ", "/home", "dashboard", 1, true);

		mockMvc.perform(post(PATH)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	void updateMenu() throws Exception {
		UpdateMenuDTO request = new UpdateMenuDTO("Catálogo", "/catalog", "inventory", 3, false);
		Mockito.when(menuService.update(Mockito.any(UpdateMenuDTO.class), Mockito.eq(1L)))
				.thenReturn(menu("Catálogo", "/catalog", "inventory", 3, false));

		mockMvc.perform(put(PATH + "/{id}", 1L)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Catálogo"))
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	@WithMockUser
	void deleteMenu() throws Exception {
		mockMvc.perform(delete(PATH + "/{id}", 1L).with(csrf()))
				.andExpect(status().isNoContent());

		Mockito.verify(menuService).deleteById(1L);
	}

	private ResponseMenuDTO menu(String name, String path, String icon, int displayOrder, boolean active) {
		ResponseMenuDTO response = new ResponseMenuDTO();
		response.setId(1L);
		response.setName(name);
		response.setPath(path);
		response.setIcon(icon);
		response.setDisplayOrder(displayOrder);
		response.setActive(active);
		return response;
	}
}
