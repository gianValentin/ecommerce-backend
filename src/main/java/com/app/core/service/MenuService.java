package com.app.core.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.core.entity.dto.menu.ResponseMenuDTO;
import com.app.core.entity.dto.menu.SaveMenuDTO;
import com.app.core.entity.dto.menu.UpdateMenuDTO;

public interface MenuService {
	Page<ResponseMenuDTO> getAll(Pageable pageable);
	List<ResponseMenuDTO> getAll();
	ResponseMenuDTO findById(Long id);
	ResponseMenuDTO save(SaveMenuDTO menu);
	ResponseMenuDTO update(UpdateMenuDTO menu, Long id);
	void deleteById(Long id);
}
