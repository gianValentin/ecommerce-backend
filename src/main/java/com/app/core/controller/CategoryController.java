package com.app.core.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.core.entity.dto.category.ResponseCategoryDTO;
import com.app.core.entity.dto.category.SaveCategoryDTO;
import com.app.core.entity.dto.category.UpdateCategoryDTO;
import com.app.core.entity.dto.user.GetUserDto;
import com.app.core.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/categories")
@Tag(name = "Category",description = "Category controller")
public class CategoryController {
	
	private final CategoryService categoryService;
	
	@Operation(description = "Get pageable endpoint for category", summary = "This is a summary for category get pageable endpoint")
	@GetMapping(value = "/pageable")
	public ResponseEntity<Page<ResponseCategoryDTO>> getPageable(Pageable pageable) {
		Page<ResponseCategoryDTO> categoryPage = categoryService.getAll(pageable);
		return ResponseEntity.status(HttpStatus.OK).body(categoryPage);
	}
	
	@Operation(description = "Get pageable endpoint for category", summary = "This is a summary for category get endpoint")
	@GetMapping
	public ResponseEntity<List<ResponseCategoryDTO>> getAll() {		
		List<ResponseCategoryDTO> categoryPage = categoryService.getAll();
		return ResponseEntity.status(HttpStatus.OK).body(categoryPage);
	}
	
	@Operation(description = "Get by id endpoint for category", summary = "This is a summary for category get by id  endpoint")
	@GetMapping("/{id}")
	public ResponseEntity<ResponseCategoryDTO> getUserById(@PathVariable Long id) {
		ResponseCategoryDTO dto = categoryService.findById(id);
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	@Operation(description = "Save  endpoint for category", summary = "This is a summary for category save endpoint")
	@PostMapping
	public ResponseEntity<ResponseCategoryDTO> save(@Valid @RequestBody SaveCategoryDTO saveCategoryDTO) {
		ResponseCategoryDTO responseCategoryDTO = categoryService.save(saveCategoryDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseCategoryDTO);
	}

	@Operation(description = "Update  endpoint for category", summary = "This is a summary for category update endpoint")
	@PutMapping(value = "/{id}")
	public ResponseEntity<ResponseCategoryDTO> update(@Valid @RequestBody UpdateCategoryDTO updateCategoryDTO,
			@PathVariable(name = "id") Long id) {
		ResponseCategoryDTO responseCategoryDTO = categoryService.update(updateCategoryDTO, id);
		return ResponseEntity.status(HttpStatus.OK).body(responseCategoryDTO);
	}

	@Operation(description = "Delete  endpoint for category", summary = "This is a summary for category delete endpoint")
	@DeleteMapping(value = "{id}")
	public ResponseEntity<GetUserDto> delete(@PathVariable(name = "id") Long id) {
		categoryService.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
