package com.app.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.app.core.entity.dto.product.ResponseProductDTO;
import com.app.core.entity.dto.product.SaveProductDTO;
import com.app.core.entity.dto.product.UpdateProductDTO;
import com.app.core.entity.model.PriceModel;
import com.app.core.entity.model.ProductModel;
import com.app.core.exception.CJNotFoundException;
import com.app.core.repository.ProductRepository;
import com.app.core.service.impl.DefaultProductService;

public class DefaultProductServiceTest {

	private static final Long CATEGORY_ID = 1L;
	private static final String PRODUCT_CODE = "PR001";

	private ProductRepository productRepository;
	private ModelMapper modelMapper;
	private DefaultProductService productService;

	private ProductModel productModel;
	private ResponseProductDTO responseProductDTO;

	@BeforeEach
	void setUp() {
		productRepository = mock(ProductRepository.class);
		modelMapper = mock(ModelMapper.class);
		productService = new DefaultProductService(productRepository, modelMapper);

		productModel = ProductModel.builder().id(1L).code(PRODUCT_CODE).name("Laptop").build();
		responseProductDTO = new ResponseProductDTO();
		responseProductDTO.setId(1L);
		responseProductDTO.setCode(PRODUCT_CODE);
		responseProductDTO.setName("Laptop");
	}

	@Test
	@DisplayName("Get all case without category id as page")
	public void getAllCaseWithoutCategoryIdAsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Mockito.when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(productModel)));
		Mockito.when(modelMapper.map(productModel, ResponseProductDTO.class)).thenReturn(responseProductDTO);

		Page<ResponseProductDTO> result = productService.getAll(null, pageable);

		assertEquals(1, result.getTotalElements());
	}

	@Test
	@DisplayName("Get all case with category id as page")
	public void getAllCaseWithCategoryIdAsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Mockito.when(productRepository.findProductByCategoryId(CATEGORY_ID, pageable))
				.thenReturn(new PageImpl<>(List.of(productModel)));
		Mockito.when(modelMapper.map(productModel, ResponseProductDTO.class)).thenReturn(responseProductDTO);

		Page<ResponseProductDTO> result = productService.getAll(CATEGORY_ID, pageable);

		assertEquals(1, result.getTotalElements());
	}

	@Test
	@DisplayName("Get all case without category id as list")
	public void getAllCaseWithoutCategoryIdAsList() {
		Mockito.when(productRepository.findAll()).thenReturn(List.of(productModel));
		Mockito.when(modelMapper.map(productModel, ResponseProductDTO.class)).thenReturn(responseProductDTO);

		List<ResponseProductDTO> result = productService.getAll((Long) null);

		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("Get all case with category id as list")
	public void getAllCaseWithCategoryIdAsList() {
		Mockito.when(productRepository.findProductByCategoryId(CATEGORY_ID)).thenReturn(List.of(productModel));
		Mockito.when(modelMapper.map(productModel, ResponseProductDTO.class)).thenReturn(responseProductDTO);

		List<ResponseProductDTO> result = productService.getAll(CATEGORY_ID);

		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("Find by id case found")
	public void findByIdCaseFound() {
		Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(productModel));
		Mockito.when(modelMapper.map(productModel, ResponseProductDTO.class)).thenReturn(responseProductDTO);

		ResponseProductDTO result = productService.findById(1L);

		assertEquals(PRODUCT_CODE, result.getCode());
	}

	@Test
	@DisplayName("Find by id case not found should throw exception")
	public void findByIdCaseNotFoundShouldThrowException() {
		Mockito.when(productRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> productService.findById(1L));
	}

	@Test
	@DisplayName("Save case success should reset price id")
	public void saveCaseSuccessShouldResetPriceId() {
		SaveProductDTO saveProductDTO = new SaveProductDTO();
		ProductModel mappedProduct = ProductModel.builder()
				.code(PRODUCT_CODE)
				.name("Laptop")
				.price(PriceModel.builder().id(99L).price(999.0).build())
				.images(Set.of())
				.build();

		Mockito.when(modelMapper.map(saveProductDTO, ProductModel.class)).thenReturn(mappedProduct);
		Mockito.when(productRepository.save(mappedProduct)).thenReturn(productModel);
		Mockito.when(modelMapper.map(productModel, ResponseProductDTO.class)).thenReturn(responseProductDTO);

		ResponseProductDTO result = productService.save(saveProductDTO);

		assertEquals(null, mappedProduct.getPrice().getId());
		assertEquals(PRODUCT_CODE, result.getCode());
	}

	@Test
	@DisplayName("Save all case success should save each product")
	public void saveAllCaseSuccessShouldSaveEachProduct() {
		SaveProductDTO saveProductDTO = new SaveProductDTO();
		ProductModel mappedProduct = ProductModel.builder().code(PRODUCT_CODE).name("Laptop").build();

		Mockito.when(modelMapper.map(saveProductDTO, ProductModel.class)).thenReturn(mappedProduct);
		Mockito.when(productRepository.save(mappedProduct)).thenReturn(productModel);
		Mockito.when(modelMapper.map(productModel, ResponseProductDTO.class)).thenReturn(responseProductDTO);

		List<ResponseProductDTO> result = productService.saveAll(List.of(saveProductDTO));

		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("Update case success should set new name")
	public void updateCaseSuccessShouldSetNewName() {
		UpdateProductDTO updateProductDTO = new UpdateProductDTO();
		updateProductDTO.setName("Laptop Pro");
		ProductModel mappedRequest = ProductModel.builder().name("Laptop Pro").build();

		Mockito.when(modelMapper.map(updateProductDTO, ProductModel.class)).thenReturn(mappedRequest);
		Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(productModel));
		Mockito.when(productRepository.save(productModel)).thenReturn(productModel);
		Mockito.when(modelMapper.map(productModel, ResponseProductDTO.class)).thenReturn(responseProductDTO);

		productService.update(updateProductDTO, 1L);

		assertEquals("Laptop Pro", productModel.getName());
	}

	@Test
	@DisplayName("Update case product not found should throw exception")
	public void updateCaseProductNotFoundShouldThrowException() {
		UpdateProductDTO updateProductDTO = new UpdateProductDTO();
		Mockito.when(modelMapper.map(updateProductDTO, ProductModel.class)).thenReturn(new ProductModel());
		Mockito.when(productRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> productService.update(updateProductDTO, 1L));
	}

	@Test
	@DisplayName("Delete by id case success")
	public void deleteByIdCaseSuccess() {
		Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(productModel));

		productService.deleteById(1L);

		verify(productRepository).delete(productModel);
	}

	@Test
	@DisplayName("Delete by id case product not found should throw exception")
	public void deleteByIdCaseProductNotFoundShouldThrowException() {
		Mockito.when(productRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> productService.deleteById(1L));
		verify(productRepository, never()).delete(Mockito.any());
	}

	@Test
	@DisplayName("Get product by code case found")
	public void getProductByCodeCaseFound() {
		Mockito.when(productRepository.findProductByCode(PRODUCT_CODE)).thenReturn(Optional.of(productModel));

		ProductModel result = productService.getProductByCode(PRODUCT_CODE);

		assertEquals(PRODUCT_CODE, result.getCode());
	}

	@Test
	@DisplayName("Get product by code case not found should throw exception")
	public void getProductByCodeCaseNotFoundShouldThrowException() {
		Mockito.when(productRepository.findProductByCode(PRODUCT_CODE)).thenReturn(Optional.empty());

		assertThrows(CJNotFoundException.class, () -> productService.getProductByCode(PRODUCT_CODE));
	}

	@Test
	@DisplayName("Is code valid case code already exists")
	public void isCodeValidCaseCodeAlreadyExists() {
		Mockito.when(productRepository.existsProductByCode(PRODUCT_CODE)).thenReturn(true);

		assertFalse(productService.isCodeValid(PRODUCT_CODE));
	}

	@Test
	@DisplayName("Is code valid case code available")
	public void isCodeValidCaseCodeAvailable() {
		Mockito.when(productRepository.existsProductByCode(PRODUCT_CODE)).thenReturn(false);

		assertTrue(productService.isCodeValid(PRODUCT_CODE));
	}
}
