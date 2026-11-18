package com.app.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.app.core.entity.dto.menu.ResponseMenuDTO;
import com.app.core.entity.dto.menu.SaveMenuDTO;
import com.app.core.entity.dto.menu.UpdateMenuDTO;
import com.app.core.entity.model.MenuModel;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.MenuRepository;
import com.app.core.service.impl.DefaultMenuService;

class DefaultMenuServiceTest {

	private MenuRepository menuRepository;
	private ModelMapper modelMapper;
	private DefaultMenuService menuService;
	private MenuModel menuModel;
	private ResponseMenuDTO responseMenuDTO;

	@BeforeEach
	void setUp() {
		menuRepository = mock(MenuRepository.class);
		modelMapper = mock(ModelMapper.class);
		menuService = new DefaultMenuService(menuRepository, modelMapper);
		menuModel = MenuModel.builder()
				.id(1L)
				.name("Resumen")
				.path("/home")
				.icon("dashboard")
				.displayOrder(1)
				.active(true)
				.build();
		responseMenuDTO = new ResponseMenuDTO();
		responseMenuDTO.setId(1L);
		responseMenuDTO.setName("Resumen");
		responseMenuDTO.setPath("/home");
		responseMenuDTO.setIcon("dashboard");
		responseMenuDTO.setDisplayOrder(1);
		responseMenuDTO.setActive(true);
	}

	@Test
	@DisplayName("Get all menus in display order")
	void getAllReturnsMenusInDisplayOrder() {
		MenuModel products = MenuModel.builder().id(2L).name("Productos").displayOrder(2).build();
		Mockito.when(menuRepository.findAllByOrderByDisplayOrderAscIdAsc()).thenReturn(List.of(menuModel, products));
		Mockito.when(modelMapper.map(menuModel, ResponseMenuDTO.class)).thenReturn(responseMenuDTO);
		ResponseMenuDTO responseProducts = new ResponseMenuDTO();
		responseProducts.setName("Productos");
		Mockito.when(modelMapper.map(products, ResponseMenuDTO.class)).thenReturn(responseProducts);

		List<ResponseMenuDTO> result = menuService.getAll();

		assertEquals(2, result.size());
		assertEquals("Resumen", result.get(0).getName());
		assertEquals("Productos", result.get(1).getName());
	}

	@Test
	@DisplayName("Get pageable menus")
	void getAllReturnsPage() {
		var pageable = PageRequest.of(0, 10);
		Page<MenuModel> page = new PageImpl<>(List.of(menuModel));
		Mockito.when(menuRepository.findAll(pageable)).thenReturn(page);
		Mockito.when(modelMapper.map(menuModel, ResponseMenuDTO.class)).thenReturn(responseMenuDTO);

		Page<ResponseMenuDTO> result = menuService.getAll(pageable);

		assertEquals(1, result.getTotalElements());
		assertEquals("Resumen", result.getContent().get(0).getName());
	}

	@Test
	@DisplayName("Find menu by id")
	void findByIdReturnsMenu() {
		Mockito.when(menuRepository.findById(1L)).thenReturn(Optional.of(menuModel));
		Mockito.when(modelMapper.map(menuModel, ResponseMenuDTO.class)).thenReturn(responseMenuDTO);

		assertEquals("/home", menuService.findById(1L).getPath());
	}

	@Test
	@DisplayName("Find menu by id throws when not found")
	void findByIdThrowsWhenNotFound() {
		Mockito.when(menuRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> menuService.findById(99L));
	}

	@Test
	@DisplayName("Save menu")
	void savePersistsMenu() {
		SaveMenuDTO request = new SaveMenuDTO("Resumen", "/home", "dashboard", 1, true);
		Mockito.when(modelMapper.map(request, MenuModel.class)).thenReturn(menuModel);
		Mockito.when(menuRepository.save(menuModel)).thenReturn(menuModel);
		Mockito.when(modelMapper.map(menuModel, ResponseMenuDTO.class)).thenReturn(responseMenuDTO);

		ResponseMenuDTO result = menuService.save(request);

		assertEquals("Resumen", result.getName());
		verify(menuRepository).save(menuModel);
	}

	@Test
	@DisplayName("Save maps every request property to the persisted menu")
	void saveMapsMenuProperties() {
		ModelMapper strictMapper = new ModelMapper();
		strictMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		MenuRepository repository = mock(MenuRepository.class);
		Mockito.when(repository.save(Mockito.any(MenuModel.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		DefaultMenuService service = new DefaultMenuService(repository, strictMapper);

		ResponseMenuDTO result = service.save(new SaveMenuDTO("Categorías", "/categories", "category", 3, true));

		assertEquals("Categorías", result.getName());
		assertEquals("/categories", result.getPath());
		assertEquals("category", result.getIcon());
		assertEquals(3, result.getDisplayOrder());
		assertEquals(true, result.isActive());
	}

	@Test
	@DisplayName("Update menu")
	void updateChangesMenuFields() {
		UpdateMenuDTO request = new UpdateMenuDTO("Catálogo", "/catalog", "inventory", 3, false);
		Mockito.when(menuRepository.findById(1L)).thenReturn(Optional.of(menuModel));
		Mockito.when(menuRepository.save(menuModel)).thenReturn(menuModel);
		Mockito.when(modelMapper.map(menuModel, ResponseMenuDTO.class)).thenAnswer(invocation -> {
			ResponseMenuDTO response = new ResponseMenuDTO();
			response.setName(menuModel.getName());
			response.setPath(menuModel.getPath());
			response.setIcon(menuModel.getIcon());
			response.setDisplayOrder(menuModel.getDisplayOrder());
			response.setActive(menuModel.isActive());
			return response;
		});

		ResponseMenuDTO result = menuService.update(request, 1L);

		assertEquals("Catálogo", result.getName());
		assertEquals("/catalog", result.getPath());
		assertEquals("inventory", result.getIcon());
		assertEquals(3, result.getDisplayOrder());
		assertEquals(false, result.isActive());
	}

	@Test
	@DisplayName("Delete menu")
	void deleteByIdDeletesExistingMenu() {
		Mockito.when(menuRepository.findById(1L)).thenReturn(Optional.of(menuModel));

		menuService.deleteById(1L);

		verify(menuRepository).delete(menuModel);
	}

	@Test
	@DisplayName("Delete menu throws when not found")
	void deleteByIdThrowsWhenNotFound() {
		Mockito.when(menuRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> menuService.deleteById(99L));
	}
}
