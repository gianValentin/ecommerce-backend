package com.app.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class DemoDataInitializationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private DataSource dataSource;

	@Test
	void seedsMasterCategoriesSubcategoriesAndProducts() {
		Integer masterCategories = jdbcTemplate.queryForObject(
				"select count(*) from category where parent_id is null", Integer.class);
		Integer subcategories = jdbcTemplate.queryForObject(
				"select count(*) from category where parent_id is not null", Integer.class);
		Integer products = jdbcTemplate.queryForObject("select count(*) from product", Integer.class);
		Integer menus = jdbcTemplate.queryForObject("select count(*) from menu", Integer.class);
		Integer activeMenus = jdbcTemplate.queryForObject("select count(*) from menu where active = true", Integer.class);
		Integer productsAssignedToSubcategories = jdbcTemplate.queryForObject("""
				select count(*)
				from product product
				join category subcategory on subcategory.id = product.category_id
				where subcategory.parent_id is not null
				""", Integer.class);
		Integer mastersWithSeededProducts = jdbcTemplate.queryForObject("""
				select count(distinct master.id)
				from category master
				join category subcategory on subcategory.parent_id = master.id
				join product product on product.category_id = subcategory.id
				where master.parent_id is null
				""", Integer.class);

		assertThat(masterCategories).isEqualTo(6);
		assertThat(subcategories).isEqualTo(12);
		assertThat(products).isEqualTo(30);
		assertThat(menus).isEqualTo(12);
		assertThat(activeMenus).isEqualTo(12);
		assertThat(jdbcTemplate.queryForObject("select name from menu where path = '/categories'", String.class))
				.isEqualTo("Categorías");
		assertThat(jdbcTemplate.queryForObject("select name from menu where path = '/menus'", String.class))
				.isEqualTo("Mantenimiento de menú");
		assertThat(productsAssignedToSubcategories).isEqualTo(30);
		assertThat(mastersWithSeededProducts).isEqualTo(6);
	}

	@Test
	void runningSeedAgainDoesNotDuplicateDemoData() {
		new ResourceDatabasePopulator(new ClassPathResource("data-h2.sql")).execute(dataSource);

		assertThat(jdbcTemplate.queryForObject("select count(*) from category", Integer.class)).isEqualTo(18);
		assertThat(jdbcTemplate.queryForObject("select count(*) from product", Integer.class)).isEqualTo(30);
		assertThat(jdbcTemplate.queryForObject("select count(*) from image", Integer.class)).isEqualTo(30);
		assertThat(jdbcTemplate.queryForObject("select count(*) from menu", Integer.class)).isEqualTo(12);
	}
}
