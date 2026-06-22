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
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.app.core.entity.dto.category.ResponseCategoryDTO;
import com.app.core.entity.dto.category.SaveCategoryDTO;
import com.app.core.entity.dto.category.UpdateCategoryDTO;
import com.app.core.entity.model.CategoryModel;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.CategoryRepository;
import com.app.core.service.impl.DefaultCategoryService;

public class DefaultCategoryServiceTest {

	private CategoryRepository categoryRepository;
	private ModelMapper modelMapper;
	private DefaultCategoryService categoryService;

	private CategoryModel categoryModel;
	private ResponseCategoryDTO responseCategoryDTO;

	@BeforeEach
	void setUp() {
		categoryRepository = mock(CategoryRepository.class);
		modelMapper = mock(ModelMapper.class);
		categoryService = new DefaultCategoryService(categoryRepository, modelMapper);

		categoryModel = CategoryModel.builder().id(1L).name("electronics").build();
		responseCategoryDTO = new ResponseCategoryDTO();
		responseCategoryDTO.setId(1L);
		responseCategoryDTO.setName("electronics");
	}

	@Test
	@DisplayName("Get all case found as page")
	public void getAllCaseFoundAsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<CategoryModel> page = new PageImpl<>(List.of(categoryModel));

		Mockito.when(categoryRepository.findAll(pageable)).thenReturn(page);
		Mockito.when(modelMapper.map(categoryModel, ResponseCategoryDTO.class)).thenReturn(responseCategoryDTO);

		Page<ResponseCategoryDTO> result = categoryService.getAll(pageable);

		assertEquals(1, result.getTotalElements());
	}

	@Test
	@DisplayName("Get all case found as list")
	public void getAllCaseFoundAsList() {
		Mockito.when(categoryRepository.findAll()).thenReturn(List.of(categoryModel));
		Mockito.when(modelMapper.map(categoryModel, ResponseCategoryDTO.class)).thenReturn(responseCategoryDTO);

		List<ResponseCategoryDTO> result = categoryService.getAll();

		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("Find by id case found")
	public void findByIdCaseFound() {
		Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));
		Mockito.when(modelMapper.map(categoryModel, ResponseCategoryDTO.class)).thenReturn(responseCategoryDTO);

		ResponseCategoryDTO result = categoryService.findById(1L);

		assertEquals("electronics", result.getName());
	}

	@Test
	@DisplayName("Find by id case not found should throw exception")
	public void findByIdCaseNotFoundShouldThrowException() {
		Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> categoryService.findById(1L));
	}

	@Test
	@DisplayName("Save case success")
	public void saveCaseSuccess() {
		SaveCategoryDTO saveCategoryDTO = new SaveCategoryDTO("electronics");

		Mockito.when(modelMapper.map(saveCategoryDTO, CategoryModel.class)).thenReturn(categoryModel);
		Mockito.when(categoryRepository.save(categoryModel)).thenReturn(categoryModel);
		Mockito.when(modelMapper.map(categoryModel, ResponseCategoryDTO.class)).thenReturn(responseCategoryDTO);

		ResponseCategoryDTO result = categoryService.save(saveCategoryDTO);

		assertEquals("electronics", result.getName());
	}

	@Test
	@DisplayName("Update case success")
	public void updateCaseSuccess() {
		UpdateCategoryDTO updateCategoryDTO = new UpdateCategoryDTO("clothing");

		Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));
		Mockito.when(categoryRepository.save(categoryModel)).thenReturn(categoryModel);
		Mockito.when(modelMapper.map(categoryModel, ResponseCategoryDTO.class)).thenReturn(responseCategoryDTO);

		categoryService.update(updateCategoryDTO, 1L);

		assertEquals("clothing", categoryModel.getName());
	}

	@Test
	@DisplayName("Update case category not found should throw exception")
	public void updateCaseCategoryNotFoundShouldThrowException() {
		UpdateCategoryDTO updateCategoryDTO = new UpdateCategoryDTO("clothing");

		Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> categoryService.update(updateCategoryDTO, 1L));
	}

	@Test
	@DisplayName("Delete by id case success")
	public void deleteByIdCaseSuccess() {
		Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryModel));

		categoryService.deleteById(1L);

		verify(categoryRepository).delete(categoryModel);
	}

	@Test
	@DisplayName("Delete by id case category not found should throw exception")
	public void deleteByIdCaseCategoryNotFoundShouldThrowException() {
		Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> categoryService.deleteById(1L));
	}
}
