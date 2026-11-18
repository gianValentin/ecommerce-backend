package com.app.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.core.entity.model.CategoryModel;

public interface CategoryRepository extends JpaRepository<CategoryModel,Long>{
	@Query(value = """
			WITH RECURSIVE category_tree(id) AS (
				SELECT id FROM category WHERE id = :categoryId
				UNION
				SELECT category.id
				FROM category
				JOIN category_tree ON category.parent_id = category_tree.id
			)
			SELECT id FROM category_tree
			""", nativeQuery = true)
	List<Long> findCategoryTreeIds(@Param("categoryId") Long categoryId);

}
