package com.app.core.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.app.core.entity.dto.menu.ResponseMenuDTO;
import com.app.core.entity.dto.menu.SaveMenuDTO;
import com.app.core.entity.dto.menu.UpdateMenuDTO;
import com.app.core.entity.model.MenuModel;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.MenuRepository;
import com.app.core.service.MenuService;
import com.app.core.utils.CustomCodeException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class DefaultMenuService implements MenuService {

	private final MenuRepository menuRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional(readOnly = true)
	public Page<ResponseMenuDTO> getAll(Pageable pageable) {
		Assert.notNull(pageable, "pageable cannot be null");
		return menuRepository.findAll(pageable).map(this::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ResponseMenuDTO> getAll() {
		return menuRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ResponseMenuDTO findById(Long id) {
		return toResponse(findMenu(id));
	}

	@Override
	@Transactional
	public ResponseMenuDTO save(SaveMenuDTO menuDTO) {
		Assert.notNull(menuDTO, "menu cannot be null");
		MenuModel menu = modelMapper.map(menuDTO, MenuModel.class);
		return toResponse(menuRepository.save(menu));
	}

	@Override
	@Transactional
	public ResponseMenuDTO update(UpdateMenuDTO menuDTO, Long id) {
		Assert.notNull(menuDTO, "menu cannot be null");
		Assert.notNull(id, "id cannot be null");

		MenuModel menu = findMenu(id);
		menu.setName(menuDTO.getName());
		menu.setPath(menuDTO.getPath());
		menu.setIcon(menuDTO.getIcon());
		menu.setDisplayOrder(menuDTO.getDisplayOrder());
		menu.setActive(menuDTO.isActive());
		return toResponse(menuRepository.save(menu));
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		Assert.notNull(id, "id cannot be null");
		menuRepository.delete(findMenu(id));
	}

	private MenuModel findMenu(Long id) {
		Assert.notNull(id, "id cannot be null");
		return menuRepository.findById(id).orElseThrow(() -> new CJNotFoundException(
				CustomCodeException.CODE_404,
				MessageFormat.format("Menu with id [{0}] not Found", id)));
	}

	private ResponseMenuDTO toResponse(MenuModel menu) {
		return modelMapper.map(menu, ResponseMenuDTO.class);
	}
}
