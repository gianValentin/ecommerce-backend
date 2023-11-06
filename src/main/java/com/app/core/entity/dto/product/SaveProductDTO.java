package com.app.core.entity.dto.product;

import java.util.Set;

import com.app.core.annotation.ProductCodeExistsConstraint;
import com.app.core.entity.dto.category.CategoryDTO;
import com.app.core.entity.dto.image.ImageDTO;
import com.app.core.entity.dto.price.PriceDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SaveProductDTO {

	@NotBlank(message = "{product.name.notBlank}")
	@Size(min = 2, max = 32, message = "{product.name.size}")
	private String name;

	@NotBlank(message = "{product.code.noBlank}")
	@Size(min = 5, max = 5, message = "{product.code.size}")
	@ProductCodeExistsConstraint(message = "{product.code.productCodeExistsConstraint}")
	private String code;

	@NotNull(message = "{lastname.notblank}")
	private CategoryDTO category;

	@NotNull(message = "{product.category.notNul}")
	private PriceDTO price;

	@NotEmpty(message = "product.images.noEmpty")
	private Set<ImageDTO> images;
}
