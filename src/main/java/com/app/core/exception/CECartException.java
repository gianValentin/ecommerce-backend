package com.app.core.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class CECartException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private String code;

	public CECartException(String code, String message) {
		super(message);
		this.code = code;
	}
}
