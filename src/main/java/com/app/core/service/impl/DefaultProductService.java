package com.app.core.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.app.core.entity.dto.product.ResponseProductDTO;
import com.app.core.entity.dto.product.SaveProductDTO;
import com.app.core.entity.dto.product.UpdateProductDTO;
import com.app.core.entity.model.ProductModel;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.ProductRepository;
import com.app.core.service.ProductService;
import com.app.core.utils.CustomCodeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class DefaultProductService implements ProductService {

	private final ProductRepository productRepository;	
	private final ModelMapper modelMapper;
	
	@Override
	@Transactional(readOnly = true)
	public Page<ResponseProductDTO> getAll(Long categoryId, Pageable pageable) {
		Assert.notNull(pageable, "pageable cannot be null");
		Page<ProductModel> products ;
		if(categoryId == null) {
			products = productRepository.findAll(pageable);
		}else {
			products = productRepository.findProductByCategoryId(categoryId, pageable);
		}
		return  products.map(product -> modelMapper.map(product, ResponseProductDTO.class));		
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ResponseProductDTO> getAll(Long categoryId) {		
		List<ProductModel> products;
		if(categoryId == null) {
			products = productRepository.findAll();
		}else {
			products = productRepository.findProductByCategoryId(categoryId);
		}
		return  products.stream().map(product -> modelMapper.map(product, ResponseProductDTO.class)).collect(Collectors.toList());		
	}
	
	@Override
	@Transactional(readOnly = true)
	public ResponseProductDTO findById(Long id) {		
		ProductModel productFound = productRepository.findById(id).orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404, MessageFormat.format("Product with id [{0}] not Found", id)));
		return modelMapper.map(productFound, ResponseProductDTO.class);
	}	

	@Override
	@Transactional
	public ResponseProductDTO save(SaveProductDTO saveProductDTO) {
		Assert.notNull(saveProductDTO, "product cannot be null");
		
		ProductModel productModel = modelMapper.map(saveProductDTO, ProductModel.class);
		
		if(ObjectUtils.allNotNull(productModel.getPrice())) {
			productModel.getPrice().setId(null);	
		}		
		
		if(!CollectionUtils.isEmpty(productModel.getImages())) {
			productModel.getImages().stream().map(image -> {
				image.setId(null);
				return image;
			});	
		}		
		ProductModel productDb = productRepository.save(productModel);
		
		return modelMapper.map(productDb, ResponseProductDTO.class);
	}
	
	@Override
	@Transactional
	public List<ResponseProductDTO> saveAll(List<SaveProductDTO> product) {
		Assert.notEmpty(product, "product list can not be empty");
		return product.stream().map(this::save).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public ResponseProductDTO update(UpdateProductDTO product, Long id) {
		Assert.notNull(product, "product cannot be null");
		Assert.notNull(id, "id cannot be null");
		
		ProductModel productRequest = modelMapper.map(product, ProductModel.class);
		ProductModel productFound = productRepository.findById(id).orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404, MessageFormat.format("Product with id [{0}] not Found", id)));
		
		productFound.setName(productRequest.getName());					
		setImages(productFound,productRequest);
		
		ProductModel productDb = productRepository.save(productFound);
		
		return modelMapper.map(productDb, ResponseProductDTO.class);
	}
	
	private void setImages(final ProductModel source, ProductModel target) {
		if(CollectionUtils.isEmpty(target.getImages())) {
			return;
		}
		
		target.getImages()
		.stream()
		.filter((image) -> image.getId() == null)
		.forEach(image -> source.addImages(image));		
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		Assert.notNull(id, "id cannot be null");
		ProductModel productFound = productRepository.findById(id).orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404, MessageFormat.format("Product with id [{0}] not Found", id)));
		productRepository.delete(productFound);
	}

	@Override
	public ProductModel getProductByCode(String code) {
		Assert.notNull(code, "code cannot be empty");
		return productRepository.findProductByCode(code).orElseThrow(() -> new CJNotFoundException(CustomCodeException.CODE_404, MessageFormat.format("Product  with id [{0}] not found",code)));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isCodeValid(String code) {		
		log.info("[Commerce] isCodeValid");
		return !productRepository.existsProductByCode(code);
	}

}
