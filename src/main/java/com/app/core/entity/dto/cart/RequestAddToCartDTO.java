package com.app.core.entity.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestAddToCartDTO {
	private Integer amount;
	private String productCode; 
}
