package com.app.core.entity.dto.price;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PriceDTO {
	private Long id;
	private BigDecimal price;	
}
