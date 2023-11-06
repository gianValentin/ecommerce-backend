package com.app.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.core.entity.model.CategoryModel;

public interface CategoryRepository extends JpaRepository<CategoryModel,Long>{

}
