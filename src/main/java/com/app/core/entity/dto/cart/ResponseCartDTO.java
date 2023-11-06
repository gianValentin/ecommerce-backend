package com.app.core.entity.dto.cart;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.app.core.entity.dto.entry.ResponseEntryDTO;
import com.app.core.entity.dto.user.ResponseUserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseCartDTO {
	private UUID id;
	private Double subTotal;
	private Double discount;
	private Double total;
	private ResponseUserDTO user;
	private Set<ResponseEntryDTO> entries;
	private Date createAt;
}