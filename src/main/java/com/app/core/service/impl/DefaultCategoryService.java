package com.app.core.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.app.core.entity.dto.category.ResponseCategoryDTO;
import com.app.core.entity.dto.category.SaveCategoryDTO;
import com.app.core.entity.dto.category.UpdateCategoryDTO;
import com.app.core.entity.model.CategoryModel;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.CategoryRepository;
import com.app.core.service.CategoryService;
import com.app.core.utils.CustomCodeException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DefaultCategoryService implements CategoryService{

	private final CategoryRepository categoryRepository;
	private final ModelMapper modelMapper;
	
	@Override
	@Transactional(readOnly = true)
	public Page<ResponseCategoryDTO> getAll(Pageable pageable) {
		Assert.notNull(pageable, "pageable cannot be null");
		Page<CategoryModel> categories = categoryRepository.findAll(pageable);
		return  categories.map(category -> modelMapper.map(category, ResponseCategoryDTO.class));		
	}

	@Override
	@Transactional(readOnly = true)
	public List<ResponseCategoryDTO> getAll() {
		List<CategoryModel> categories = categoryRepository.findAll();
		return  categories.stream().map(category -> modelMapper.map(category, ResponseCategoryDTO.class)).collect(Collectors.toList());		
	}

	@Override
	@Transactional(readOnly = true)
	public ResponseCategoryDTO findById(Long id) {
		CategoryModel categoryFound = categoryRepository.findById(id).orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404, MessageFormat.format("Category with id [{0}] not Found", id)));
		return modelMapper.map(categoryFound, ResponseCategoryDTO.class);
	}

	@Override
	@Transactional
	public ResponseCategoryDTO save(SaveCategoryDTO categoryDTO) {
		Assert.notNull(categoryDTO, "category cannot be null");
		
		CategoryModel categoryModel = modelMapper.map(categoryDTO, CategoryModel.class);
		
		CategoryModel categoryDb = categoryRepository.save(categoryModel);
		
		return modelMapper.map(categoryDb, ResponseCategoryDTO.class);
	}

	@Override
	@Transactional
	public ResponseCategoryDTO update(UpdateCategoryDTO categoryDTO, Long id) {
		Assert.notNull(categoryDTO, "category cannot be null");
		Assert.notNull(id, "id cannot be null");
		
		CategoryModel categoryFound= categoryRepository.findById(id).orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404, MessageFormat.format("Category with id [{0}] not Found", id)));
		
		categoryFound.setName(categoryDTO.getName());		
		
		CategoryModel categoryUpdated= categoryRepository.save(categoryFound);
		
		return modelMapper.map(categoryUpdated, ResponseCategoryDTO.class);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		Assert.notNull(id, "id cannot be null");
		CategoryModel categoryFound = categoryRepository.findById(id).orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404, MessageFormat.format("Category with id [{0}] not Found", id)));
		categoryRepository.delete(categoryFound);
	}

}
