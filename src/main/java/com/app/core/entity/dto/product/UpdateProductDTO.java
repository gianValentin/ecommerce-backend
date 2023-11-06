package com.app.core.entity.dto.product;

import java.util.Set;

import com.app.core.entity.dto.category.CategoryDTO;
import com.app.core.entity.dto.image.ImageDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateProductDTO {
	private String name;
	private String code;
	private CategoryDTO category;
	private Set<ImageDTO> images;
}
