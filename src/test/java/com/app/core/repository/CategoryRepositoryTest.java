package com.app.core.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.transaction.annotation.Transactional;

import com.app.core.entity.model.CategoryModel;

/**
 * Usa el datasource definido por el perfil Maven activo (h2 o postgresql),
 * tal como lo hace la aplicacion en runtime. Ejecutar con:
 * ./mvnw test -Ph2 (default) o ./mvnw test -Ppostgresql
 */
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryRepositoryTest {

	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private TestEntityManager testEntityManager;

	private Long categoryElectronicsId;

	@BeforeEach
	void setUp() {
		categoryRepository.deleteAll();
		persistDemoCategories();
	}

	private void persistDemoCategories() {
		CategoryModel electronics = CategoryModel.builder().name("electronics").build();
		CategoryModel clothing = CategoryModel.builder().name("clothing").build();

		testEntityManager.persist(electronics);
		testEntityManager.persist(clothing);

		categoryElectronicsId = electronics.getId();
	}

	@Test
	@DisplayName("Find by id case found")
	public void findByIdCaseFound() {
		Optional<CategoryModel> categoryDb = categoryRepository.findById(categoryElectronicsId);

		assertTrue(categoryDb.isPresent());
		assertThat(categoryDb.get().getName()).isEqualTo("electronics");
	}

	@Test
	@DisplayName("Find by id case not found")
	public void findByIdCaseNotFound() {
		Optional<CategoryModel> categoryDb = categoryRepository.findById(Long.MAX_VALUE);

		assertTrue(categoryDb.isEmpty());
	}

	@Test
	@DisplayName("Find all case found all categories")
	public void findAllCaseFoundAllCategories() {
		List<CategoryModel> categories = categoryRepository.findAll();

		assertThat(categories).isNotNull();
		assertThat(categories.size()).isEqualTo(2);
	}

	@Test
	@DisplayName("Save category case success")
	public void saveCategoryCaseSuccess() {
		CategoryModel categoryToSave = CategoryModel.builder().name("furniture").build();

		CategoryModel categorySaved = categoryRepository.save(categoryToSave);

		assertThat(categorySaved).isNotNull();
		assertThat(categorySaved.getId()).isNotNull();
		assertThat(categorySaved.getCreateAt()).isNotNull();
	}

	@Test
	@DisplayName("Save category case error")
	public void saveCategoryCaseError() {
		Exception exception = assertThrows(RuntimeException.class, () -> categoryRepository.save(null));

		assertTrue(exception.getMessage().contains("Entity must not be null"));
	}

	@Test
	@DisplayName("Delete category case success")
	public void deleteCategoryCaseSuccess() {
		categoryRepository.deleteById(categoryElectronicsId);

		Optional<CategoryModel> categoryDb = categoryRepository.findById(categoryElectronicsId);

		assertTrue(categoryDb.isEmpty());
	}
}
