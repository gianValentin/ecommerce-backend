package com.app.core.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.app.core.entity.model.CategoryModel;
import com.app.core.entity.model.PriceModel;
import com.app.core.entity.model.ProductModel;

/**
 * Usa el datasource definido por el perfil Maven activo (h2 o postgresql),
 * tal como lo hace la aplicacion en runtime. Ejecutar con:
 * ./mvnw test -Ph2 (default) o ./mvnw test -Ppostgresql
 */
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {

	private static final String PRODUCT_CODE = "PR001";

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private TestEntityManager testEntityManager;

	private CategoryModel category;

	@BeforeEach
	void setUp() {
		productRepository.deleteAll();
		category = testEntityManager.persist(CategoryModel.builder().name("electronics").build());
		persistDemoProduct(PRODUCT_CODE, "Laptop", category);
	}

	private ProductModel persistDemoProduct(String code, String name, CategoryModel categoryModel) {
		PriceModel price = PriceModel.builder().price(999.0).build();
		ProductModel product = ProductModel.builder()
				.code(code)
				.name(name)
				.category(categoryModel)
				.price(price)
				.build();

		return testEntityManager.persist(product);
	}

	@Test
	@DisplayName("Find product by code case found")
	public void findProductByCodeCaseFound() {
		Optional<ProductModel> productDb = productRepository.findProductByCode(PRODUCT_CODE);

		assertTrue(productDb.isPresent());
		assertThat(productDb.get().getName()).isEqualTo("Laptop");
	}

	@Test
	@DisplayName("Find product by code case not found")
	public void findProductByCodeCaseNotFound() {
		Optional<ProductModel> productDb = productRepository.findProductByCode("UNKNOWN");

		assertTrue(productDb.isEmpty());
	}

	@Test
	@DisplayName("Exists product by code case true")
	public void existsProductByCodeCaseTrue() {
		assertTrue(productRepository.existsProductByCode(PRODUCT_CODE));
	}

	@Test
	@DisplayName("Exists product by code case false")
	public void existsProductByCodeCaseFalse() {
		assertFalse(productRepository.existsProductByCode("UNKNOWN"));
	}

	@Test
	@DisplayName("Find product by category id case found as list")
	public void findProductByCategoryIdCaseFoundAsList() {
		List<ProductModel> products = productRepository.findProductByCategoryId(category.getId());

		assertThat(products).hasSize(1);
		assertThat(products.get(0).getCode()).isEqualTo(PRODUCT_CODE);
	}

	@Test
	@DisplayName("Find product by category id case not found as list")
	public void findProductByCategoryIdCaseNotFoundAsList() {
		List<ProductModel> products = productRepository.findProductByCategoryId(Long.MAX_VALUE);

		assertThat(products).isEmpty();
	}

	@Test
	@DisplayName("Find product by category id case found as page")
	public void findProductByCategoryIdCaseFoundAsPage() {
		Page<ProductModel> products = productRepository.findProductByCategoryId(category.getId(), PageRequest.of(0, 10));

		assertThat(products.getTotalElements()).isEqualTo(1);
	}

	@Test
	@DisplayName("Save product case success")
	public void saveProductCaseSuccess() {
		ProductModel newProduct = ProductModel.builder()
				.code("PR002")
				.name("Mouse")
				.category(category)
				.price(PriceModel.builder().price(20.0).build())
				.build();

		ProductModel savedProduct = productRepository.save(newProduct);

		assertThat(savedProduct).isNotNull();
		assertThat(savedProduct.getId()).isNotNull();
	}

	@Test
	@DisplayName("Delete product case success")
	public void deleteProductCaseSuccess() {
		ProductModel productDb = productRepository.findProductByCode(PRODUCT_CODE).orElseThrow();

		productRepository.deleteById(productDb.getId());

		assertTrue(productRepository.findProductByCode(PRODUCT_CODE).isEmpty());
	}
}
