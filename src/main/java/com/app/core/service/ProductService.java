package com.app.core.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.core.entity.dto.product.ResponseProductDTO;
import com.app.core.entity.dto.product.SaveProductDTO;
import com.app.core.entity.dto.product.UpdateProductDTO;
import com.app.core.entity.model.ProductModel;

public interface ProductService {
	Page<ResponseProductDTO> getAll(Long categoryId, Pageable pageable);
	List<ResponseProductDTO> getAll(Long categoryId);
	ResponseProductDTO findById(final Long id);
	ResponseProductDTO save(final SaveProductDTO product);
	List<ResponseProductDTO>saveAll(final List<SaveProductDTO> product);
	ResponseProductDTO update(final UpdateProductDTO product, final Long id);
	void deleteById(final Long id);	
	ProductModel getProductByCode(String code);
	boolean isCodeValid(String value);
}
