package com.app.core.entity.dto.image;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseImageDTO {
	private Long id;
	private String url;
	private Date createAt;	
}
