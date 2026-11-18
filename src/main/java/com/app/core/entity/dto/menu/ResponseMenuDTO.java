package com.app.core.entity.dto.menu;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseMenuDTO {
	private Long id;
	private String name;
	private String path;
	private String icon;
	private Integer displayOrder;
	private boolean active;
	private Date createAt;
}
