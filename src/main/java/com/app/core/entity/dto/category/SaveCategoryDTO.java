package com.app.core.entity.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SaveCategoryDTO {
	private String name;
	private Long parentId;

	public SaveCategoryDTO(String name) {
		this.name = name;
	}
}
