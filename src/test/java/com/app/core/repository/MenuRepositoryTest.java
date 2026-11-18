package com.app.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import com.app.core.entity.model.MenuModel;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MenuRepositoryTest {

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private TestEntityManager testEntityManager;

	@Test
	void persistsMenuAndOrdersByDisplayOrder() {
		menuRepository.deleteAll();
		MenuModel products = MenuModel.builder()
				.name("Productos")
				.path("/test-products")
				.icon("inventory")
				.displayOrder(2)
				.active(true)
				.build();
		MenuModel overview = MenuModel.builder()
				.name("Resumen")
				.path("/test-home")
				.icon("dashboard")
				.displayOrder(1)
				.active(true)
				.build();
		testEntityManager.persist(products);
		testEntityManager.persist(overview);
		testEntityManager.flush();
		testEntityManager.clear();

		var menus = menuRepository.findAllByOrderByDisplayOrderAscIdAsc();

		assertThat(menus).extracting(MenuModel::getName).containsExactly("Resumen", "Productos");
		assertThat(menus.get(0).getCreateAt()).isNotNull();
	}
}
