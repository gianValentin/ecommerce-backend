package com.app.core.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.app.core.service.MenuService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestPropertySource(properties = "security.allowedOrigins=http://localhost:4200")
class SecurityAuthorizationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MenuService menuService;

	@Test
	void anonymousRequestCannotReadAdminMenus() throws Exception {
		mockMvc.perform(get("/api/v1/menus"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void regularUserCannotReadAdminMenus() throws Exception {
		mockMvc.perform(get("/api/v1/menus"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void administratorCanReadAdminMenus() throws Exception {
		when(menuService.getAll()).thenReturn(List.of());
		mockMvc.perform(get("/api/v1/menus"))
				.andExpect(status().isOk());
	}
}
