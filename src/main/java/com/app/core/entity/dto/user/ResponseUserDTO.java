package com.app.core.entity.dto.user;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUserDTO {
	UUID id;		
	String firstname;		
	String username;		
	String password;		
	String lastname;	
	String email;
	Date createAt;
}
