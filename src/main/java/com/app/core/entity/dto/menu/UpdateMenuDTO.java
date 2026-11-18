package com.app.core.entity.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateMenuDTO {
	@NotBlank(message = "El nombre del menú es obligatorio")
	@Size(max = 100, message = "El nombre del menú no puede superar 100 caracteres")
	private String name;

	@NotBlank(message = "La ruta del menú es obligatoria")
	@Size(max = 255, message = "La ruta del menú no puede superar 255 caracteres")
	private String path;

	@NotBlank(message = "El icono del menú es obligatorio")
	@Size(max = 80, message = "El icono del menú no puede superar 80 caracteres")
	private String icon;

	@NotNull(message = "El orden del menú es obligatorio")
	@PositiveOrZero(message = "El orden del menú debe ser mayor o igual a cero")
	private Integer displayOrder;

	private boolean active;
}
