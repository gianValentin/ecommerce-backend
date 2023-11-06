package com.app.core.entity.dto.entry;

import java.util.Date;

import com.app.core.entity.dto.product.ResponseProductDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseEntryDTO {
	private Long id;
	private Integer amount;
	private Double total;
	private ResponseProductDTO product;
	private Date createAt;
	
}
