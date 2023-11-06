package com.app.core.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.core.entity.dto.category.ResponseCategoryDTO;
import com.app.core.entity.dto.category.SaveCategoryDTO;
import com.app.core.entity.dto.category.UpdateCategoryDTO;

public interface CategoryService {
	Page<ResponseCategoryDTO> getAll(Pageable pageable);
	List<ResponseCategoryDTO> getAll();
	ResponseCategoryDTO findById(final Long id);
	ResponseCategoryDTO save(final SaveCategoryDTO category);
	ResponseCategoryDTO update(final UpdateCategoryDTO catgory, final Long id);
	void deleteById(final Long id);	
}
