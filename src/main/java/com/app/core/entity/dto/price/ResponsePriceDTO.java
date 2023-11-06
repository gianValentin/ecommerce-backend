package com.app.core.entity.dto.price;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponsePriceDTO {
	private Long id;
	private Double price;	
	private Date createAt;
}
