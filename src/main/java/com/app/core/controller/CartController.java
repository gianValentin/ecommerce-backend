package com.app.core.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.core.entity.dto.cart.RequestAddToCartDTO;
import com.app.core.entity.dto.cart.RequestRemoveEntryCartDTO;
import com.app.core.entity.dto.cart.ResponseCartDTO;
import com.app.core.entity.model.CartModel;
import com.app.core.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/cart")
@Tag(name = "Cart", description = "Cart controller")
public class CartController {
		
	private final CartService cartService;
	private final ModelMapper modelMapper;
	
	@Operation(description = "Get cart endpoint", summary = "This is a summary for cart get  endpoint")
	@GetMapping("/{id}")
	public ResponseEntity<ResponseCartDTO> getCart(@PathVariable String id) {
		CartModel cart = cartService.getCart(id);
		
		if(cart == null)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		
		ResponseCartDTO dto = modelMapper.map(cart, ResponseCartDTO.class);
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}

	
	@Operation(description = "Add to cart endpoint", summary = "This is a summary for add to  cart endpoint")
	@PostMapping("/{id}/addToCart")
	public ResponseEntity<ResponseCartDTO> addToCart(@Valid @RequestBody RequestAddToCartDTO req,@PathVariable String id) {
		
		CartModel cart = cartService.getCart(id);
		String code = req.getProductCode();
		Integer amount = req.getAmount();
		
		CartModel cartModify = cartService.addToCard(cart, code, amount);
		
		ResponseCartDTO dto = modelMapper.map(cartModify, ResponseCartDTO.class);
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	@Operation(description = "remove entry endpoint", summary = "This is a summary for remove a entry of cart endpoint")
	@DeleteMapping("/{id}/entry")
	public ResponseEntity<?> removeEntry(@Valid @RequestBody RequestRemoveEntryCartDTO req, @PathVariable String id) {
		
		// validate cart if exists by id, if not exists throw exception for 404 status
		cartService.validateIfExistsCartById(id);
		
		// get only cart exists in database
		CartModel cart = cartService.getCart(id);				
		Long entryId = req.getEntryId();
		
		CartModel cartModify = cartService.removeEntry(cart, entryId);
		
		ResponseCartDTO dto = modelMapper.map(cartModify, ResponseCartDTO.class);
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}	
}
