package com.app.core.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.core.entity.model.ProductModel;

public interface ProductRepository  extends JpaRepository<ProductModel, Long>{
	Optional<ProductModel> findProductByCode(String code);
	
	@Query(value = """
			select p from ProductModel p inner join CategoryModel  c\s
			on p.category.id = c.id\s
			where c.id = :categoryId \s
			""")
	Page<ProductModel> findProductByCategoryId(Long categoryId, Pageable pageable);
	
	@Query(value = """
			select p from ProductModel p inner join CategoryModel  c\s
			on p.category.id = c.id\s
			where c.id = :categoryId \s
			""")
	List<ProductModel> findProductByCategoryId(Long categoryId);

	@Query("select p from ProductModel p where p.category.id in :categoryIds")
	Page<ProductModel> findProductsByCategoryIds(@Param("categoryIds") Collection<Long> categoryIds, Pageable pageable);

	@Query("select p from ProductModel p where p.category.id in :categoryIds")
	List<ProductModel> findProductsByCategoryIds(@Param("categoryIds") Collection<Long> categoryIds);

	boolean existsProductByCode(String code);
}
