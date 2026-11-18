package com.app.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.core.entity.model.MenuModel;

public interface MenuRepository extends JpaRepository<MenuModel, Long> {
	List<MenuModel> findAllByOrderByDisplayOrderAscIdAsc();
}
