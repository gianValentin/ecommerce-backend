package com.app.core.entity.dto.product;

import java.util.Set;

import com.app.core.entity.dto.category.CategoryDTO;
import com.app.core.entity.dto.image.ImageDTO;
import com.app.core.entity.dto.price.PriceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestProductDTO {
	private String name;
	private String code;
	private CategoryDTO category;
	private PriceDTO price;
	private Set<ImageDTO> images;
}
