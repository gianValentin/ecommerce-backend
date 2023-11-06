package com.app.core.entity.dto.category;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseCategoryDTO {
	private Long id;
	private String name;
	private Date createAt;
}