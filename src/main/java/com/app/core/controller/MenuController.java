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

import com.app.core.entity.dto.menu.ResponseMenuDTO;
import com.app.core.entity.dto.menu.SaveMenuDTO;
import com.app.core.entity.dto.menu.UpdateMenuDTO;
import com.app.core.service.MenuService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/menus")
@Tag(name = "Menu", description = "Menu maintenance controller")
public class MenuController {

	private final MenuService menuService;

	@Operation(summary = "Get paginated menus")
	@GetMapping("/pageable")
	public ResponseEntity<Page<ResponseMenuDTO>> getPageable(Pageable pageable) {
		return ResponseEntity.ok(menuService.getAll(pageable));
	}

	@Operation(summary = "Get all menus")
	@GetMapping
	public ResponseEntity<List<ResponseMenuDTO>> getAll() {
		return ResponseEntity.ok(menuService.getAll());
	}

	@Operation(summary = "Get a menu by id")
	@GetMapping("/{id}")
	public ResponseEntity<ResponseMenuDTO> getById(@PathVariable Long id) {
		return ResponseEntity.ok(menuService.findById(id));
	}

	@Operation(summary = "Create a menu")
	@PostMapping
	public ResponseEntity<ResponseMenuDTO> save(@Valid @RequestBody SaveMenuDTO menu) {
		return ResponseEntity.status(HttpStatus.CREATED).body(menuService.save(menu));
	}

	@Operation(summary = "Update a menu")
	@PutMapping("/{id}")
	public ResponseEntity<ResponseMenuDTO> update(@Valid @RequestBody UpdateMenuDTO menu, @PathVariable Long id) {
		return ResponseEntity.ok(menuService.update(menu, id));
	}

	@Operation(summary = "Delete a menu")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		menuService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
