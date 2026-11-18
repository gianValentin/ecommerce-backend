INSERT INTO _user(dtype,id,create_at,email,firstname,lastname,password,role,username)
SELECT 'SecurityUser',random_uuid(),now(),'admin@admin.com','admin','admin','$2a$10$0hcG7w1vstFYfSwJ7G5sjeJomt6x9typiDI0uFJrCJfnl4FrQ6/CK','ADMIN','admin'
WHERE NOT EXISTS (
    SELECT 1 FROM _user WHERE username = 'admin'
);

INSERT INTO category (name, create_at)
SELECT seed.name, now()
FROM (VALUES
    ('Electrónica'),
    ('Hogar y cocina'),
    ('Deportes'),
    ('Moda'),
    ('Belleza'),
    ('Juguetes')
) AS seed(name)
WHERE NOT EXISTS (
    SELECT 1
    FROM category existing_category
    WHERE existing_category.name = seed.name
      AND existing_category.parent_id IS NULL
);

INSERT INTO category (name, create_at, parent_id)
SELECT 'Telefonía y wearables', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Electrónica'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Telefonía y wearables'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Computación y audio', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Electrónica'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Computación y audio'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Electrodomésticos', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Hogar y cocina'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Electrodomésticos'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Hogar y descanso', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Hogar y cocina'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Hogar y descanso'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Entrenamiento', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Deportes'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Entrenamiento'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Deportes al aire libre', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Deportes'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Deportes al aire libre'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Ropa', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Moda'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Ropa'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Accesorios', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Moda'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Accesorios'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Cuidado personal', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Belleza'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Cuidado personal'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Perfumería', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Belleza'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Perfumería'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Construcción y lógica', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Juguetes'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Construcción y lógica'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO category (name, create_at, parent_id)
SELECT 'Juguetes y peluches', now(), parent_category.id
FROM category parent_category
WHERE parent_category.name = 'Juguetes'
  AND parent_category.parent_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM category existing_category
      WHERE existing_category.name = 'Juguetes y peluches'
        AND existing_category.parent_id = parent_category.id
  );

INSERT INTO menu (name, path, icon, display_order, active, create_at)
SELECT seed.name, seed.path, seed.icon, seed.display_order, TRUE, now()
FROM (VALUES
    ('Resumen', '/home', 'dashboard', 1),
    ('Usuarios', '/users', 'group', 2),
    ('Categorías', '/categories', 'category', 3),
    ('Productos', '/products', 'inventory_2', 4),
    ('Órdenes', '/orders', 'shopping_bag', 5),
    ('Carrito de compra', '/carts', 'shopping_cart', 6),
    ('Inventario', '/inventory', 'warehouse', 7),
    ('Promoción', '/promotions', 'sell', 8),
    ('Analítica', '/analytics', 'monitoring', 9),
    ('Configuración', '/settings', 'settings', 10),
    ('Ayuda y soporte', '/support', 'help', 11),
    ('Mantenimiento de menú', '/menus', 'menu', 12)
) AS seed(name, path, icon, display_order)
WHERE NOT EXISTS (
    SELECT 1 FROM menu existing_menu WHERE existing_menu.path = seed.path
);

INSERT INTO price (price, create_at)
SELECT seed.price, now()
FROM (VALUES
    ('SKU-001', 499.99),
    ('SKU-002', 79.99),
    ('SKU-003', 129.99),
    ('SKU-004', 899.99),
    ('SKU-005', 59.99),
    ('SKU-006', 49.99),
    ('SKU-007', 39.99),
    ('SKU-008', 29.99),
    ('SKU-009', 54.99),
    ('SKU-010', 89.99),
    ('SKU-011', 24.99),
    ('SKU-012', 31.99),
    ('SKU-013', 74.99),
    ('SKU-014', 44.99),
    ('SKU-015', 42.99),
    ('SKU-016', 19.99),
    ('SKU-017', 99.99),
    ('SKU-018', 52.99),
    ('SKU-019', 34.99),
    ('SKU-020', 17.99),
    ('SKU-021', 22.99),
    ('SKU-022', 12.99),
    ('SKU-023', 64.99),
    ('SKU-024', 48.99),
    ('SKU-025', 41.99),
    ('SKU-026', 36.99),
    ('SKU-027', 46.99),
    ('SKU-028', 14.99),
    ('SKU-029', 27.99),
    ('SKU-030', 21.99)
) AS seed(code, price)
WHERE NOT EXISTS (
    SELECT 1 FROM product existing_product WHERE existing_product.code = seed.code
)
AND NOT EXISTS (
    SELECT 1 FROM price existing_price WHERE existing_price.price = seed.price
);

INSERT INTO product (code, name, create_at, category_id, price_id)
SELECT seed.code, seed.name, now(), seed_category.id, seed_price.id
FROM (VALUES
    ('SKU-001', 'Teléfono inteligente Nova X', 'Telefonía y wearables', 499.99),
    ('SKU-002', 'Auriculares inalámbricos Pulse', 'Computación y audio', 79.99),
    ('SKU-003', 'Reloj inteligente Active', 'Telefonía y wearables', 129.99),
    ('SKU-004', 'Laptop portátil Air 14', 'Computación y audio', 899.99),
    ('SKU-005', 'Altavoz Bluetooth Mini', 'Computación y audio', 59.99),
    ('SKU-006', 'Cafetera de filtro Classic', 'Electrodomésticos', 49.99),
    ('SKU-007', 'Licuadora compacta Mix', 'Electrodomésticos', 39.99),
    ('SKU-008', 'Lámpara de escritorio LED', 'Hogar y descanso', 29.99),
    ('SKU-009', 'Juego de sábanas Queen', 'Hogar y descanso', 54.99),
    ('SKU-010', 'Juego de ollas de acero', 'Electrodomésticos', 89.99),
    ('SKU-011', 'Mat de yoga antideslizante', 'Entrenamiento', 24.99),
    ('SKU-012', 'Balón de baloncesto Pro', 'Deportes al aire libre', 31.99),
    ('SKU-013', 'Zapatillas para correr', 'Deportes al aire libre', 74.99),
    ('SKU-014', 'Casco de ciclismo seguro', 'Deportes al aire libre', 44.99),
    ('SKU-015', 'Mancuernas ajustables 5 kg', 'Entrenamiento', 42.99),
    ('SKU-016', 'Camiseta básica de algodón', 'Ropa', 19.99),
    ('SKU-017', 'Chaqueta urbana ligera', 'Ropa', 99.99),
    ('SKU-018', 'Mochila clásica diaria', 'Accesorios', 52.99),
    ('SKU-019', 'Gafas de sol estilo retro', 'Accesorios', 34.99),
    ('SKU-020', 'Gorra deportiva ajustable', 'Accesorios', 17.99),
    ('SKU-021', 'Crema hidratante facial', 'Cuidado personal', 22.99),
    ('SKU-022', 'Champú nutritivo', 'Cuidado personal', 12.99),
    ('SKU-023', 'Perfume Brisa 50 ml', 'Perfumería', 64.99),
    ('SKU-024', 'Secador de cabello compacto', 'Cuidado personal', 48.99),
    ('SKU-025', 'Set de cuidado de la piel', 'Cuidado personal', 41.99),
    ('SKU-026', 'Bloques de construcción 300 piezas', 'Construcción y lógica', 36.99),
    ('SKU-027', 'Auto a control remoto', 'Juguetes y peluches', 46.99),
    ('SKU-028', 'Rompecabezas de 500 piezas', 'Construcción y lógica', 14.99),
    ('SKU-029', 'Muñeca Aventuras', 'Juguetes y peluches', 27.99),
    ('SKU-030', 'Peluche de oso clásico', 'Juguetes y peluches', 21.99)
) AS seed(code, name, category_name, price)
JOIN category seed_category ON seed_category.name = seed.category_name
    AND seed_category.parent_id IS NOT NULL
JOIN price seed_price ON seed_price.price = seed.price
WHERE NOT EXISTS (
    SELECT 1 FROM product existing_product WHERE existing_product.code = seed.code
);

INSERT INTO image (url, images_id, create_at)
SELECT 'https://placehold.co/600x600/png?text=' || seed.code, seeded_product.id, now()
FROM (VALUES
    ('SKU-001'), ('SKU-002'), ('SKU-003'), ('SKU-004'), ('SKU-005'),
    ('SKU-006'), ('SKU-007'), ('SKU-008'), ('SKU-009'), ('SKU-010'),
    ('SKU-011'), ('SKU-012'), ('SKU-013'), ('SKU-014'), ('SKU-015'),
    ('SKU-016'), ('SKU-017'), ('SKU-018'), ('SKU-019'), ('SKU-020'),
    ('SKU-021'), ('SKU-022'), ('SKU-023'), ('SKU-024'), ('SKU-025'),
    ('SKU-026'), ('SKU-027'), ('SKU-028'), ('SKU-029'), ('SKU-030')
) AS seed(code)
JOIN product seeded_product ON seeded_product.code = seed.code
WHERE NOT EXISTS (
    SELECT 1 FROM image existing_image
    WHERE existing_image.images_id = seeded_product.id
);
