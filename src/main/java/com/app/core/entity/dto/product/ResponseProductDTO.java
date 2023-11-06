package com.app.core.entity.dto.product;

import java.util.Date;
import java.util.Set;
import com.app.core.entity.dto.category.ResponseCategoryDTO;
import com.app.core.entity.dto.image.ResponseImageDTO;
import com.app.core.entity.dto.price.ResponsePriceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseProductDTO {
	private Long id;
	private String code;
	private String name;
	private Date createAt;
	private ResponseCategoryDTO category;
	private ResponsePriceDTO price;
	private Set<ResponseImageDTO> images;
}
