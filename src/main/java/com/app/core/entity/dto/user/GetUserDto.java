package com.app.core.entity.dto.user;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GetUserDto{
		UUID id;		
		String firstname;		
		String username;	
		String lastname;	
		String email;
		Date createAt;

}
