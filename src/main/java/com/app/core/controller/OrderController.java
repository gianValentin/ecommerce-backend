package com.app.core.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.core.entity.dto.cart.ResponseCartDTO;
import com.app.core.entity.model.CartModel;
import com.app.core.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/order")
@Tag(name = "Order", description = "Order controller")
public class OrderController {
	
	private final OrderService orderService;
	private final ModelMapper modelMapper;
	
	@Operation(description = "Get pageable endpoint for order", summary = "This is a summary for order get all pageable endpoint")
	@GetMapping(value = "/pageable")
	public ResponseEntity<Page<ResponseCartDTO>> getPageable(Pageable pageable) {
		Page<ResponseCartDTO> carstOrderPage = orderService.getAll(pageable);
		return ResponseEntity.status(HttpStatus.OK).body(carstOrderPage);
	}
	
	@Operation(description = "Get pageable endpoint for order", summary = "This is a summary for order get all endpoint")
	@GetMapping
	public ResponseEntity<List<ResponseCartDTO>> getAll() {		
		List<ResponseCartDTO> cartsOrder = orderService.getAll();
		return ResponseEntity.status(HttpStatus.OK).body(cartsOrder);
	}
	
	@Operation(description = "Get find by id endpoint for order", summary = "This is a summary for order get by id endpoint")
	@GetMapping(value = "/{orderId}")
	public ResponseEntity<ResponseCartDTO> getById(@PathVariable String orderId) {		
		ResponseCartDTO cartsOrder = orderService.getById(orderId);
		return ResponseEntity.status(HttpStatus.OK).body(cartsOrder);
	}
	
	
	@Operation(description = "Place order endpoint", summary = "This is a summary for place order endpoint")
	@PostMapping("/placeOrder")
	public ResponseEntity<ResponseCartDTO> placeOrder() {		
		CartModel cartOrder = orderService.placeOrder();
		ResponseCartDTO dto = modelMapper.map(cartOrder, ResponseCartDTO.class);
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
}
