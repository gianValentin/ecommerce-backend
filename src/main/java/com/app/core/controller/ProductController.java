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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.core.entity.dto.ValidList;
import com.app.core.entity.dto.product.ResponseProductDTO;
import com.app.core.entity.dto.product.SaveProductDTO;
import com.app.core.entity.dto.product.UpdateProductDTO;
import com.app.core.entity.dto.user.GetUserDto;
import com.app.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/product")
@Tag(name = "Product",description = "Product controller")
public class ProductController {
	
	private final ProductService productService;
	
	@Operation(description = "Get pageable endpoint for product", summary = "This is a summary for product get pageable endpoint")
	@GetMapping(value = "/pageable")
	public ResponseEntity<Page<ResponseProductDTO>> getPageable(@RequestParam (required = false) Long categoryId, Pageable pageable) {
		Page<ResponseProductDTO> productPage = productService.getAll(categoryId, pageable);
		return ResponseEntity.status(HttpStatus.OK).body(productPage);
	}
	
	@Operation(description = "Get pageable endpoint for product", summary = "This is a summary for product get endpoint")
	@GetMapping
	public ResponseEntity<List<ResponseProductDTO>> getAll(@RequestParam (required = false) Long categoryId) {		
		List<ResponseProductDTO> productPage = productService.getAll(categoryId);
		return ResponseEntity.status(HttpStatus.OK).body(productPage);
	}
	
	@Operation(description = "Get by id endpoint for user", summary = "This is a summary for user get by id  endpoint")
	@GetMapping("/{id}")
	public ResponseEntity<ResponseProductDTO> getUserById(@PathVariable Long id) {
		ResponseProductDTO dto = productService.findById(id);
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	@Operation(description = "Save  endpoint for product", summary = "This is a summary for product save endpoint")
	@PostMapping
	public ResponseEntity<List<ResponseProductDTO>> save(@Valid @RequestBody  ValidList< SaveProductDTO> saveProductListDTO) {
		List<ResponseProductDTO> responseProductsDTO = productService.saveAll(saveProductListDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseProductsDTO);
	}

	@Operation(description = "Update  endpoint for product", summary = "This is a summary for product update endpoint")
	@PutMapping(value = "/{id}")
	public ResponseEntity<ResponseProductDTO> update(@Valid @RequestBody UpdateProductDTO updateProductDTO,
			@PathVariable(name = "id") Long id) {
		ResponseProductDTO responseProductDTO = productService.update(updateProductDTO, id);
		return ResponseEntity.status(HttpStatus.OK).body(responseProductDTO);
	}

	@Operation(description = "Delete  endpoint for product", summary = "This is a summary for product delete endpoint")
	@DeleteMapping(value = "{id}")
	public ResponseEntity<GetUserDto> delete(@PathVariable(name = "id") Long id) {
		productService.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
