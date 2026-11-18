CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO _user(dtype,id,create_at,email,firstname,lastname,password,role,username) 
SELECT 'SecurityUser',uuid_generate_v1(),now(),'admin@admin.com','admin','admin','$2a$10$Gd6JJCKDvDQz30J9TXq6HeHVeAj8wPLd.ctPO4nwZetlO2pE0H5F.','ADMIN','admin'
WHERE
NOT EXISTS (
SELECT username FROM _user WHERE username = 'admin'
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
    SELECT 1 FROM category existing_category
    WHERE existing_category.name = seed.name
      AND existing_category.parent_id IS NULL
);

INSERT INTO category (name, create_at, parent_id)
SELECT seed.name, now(), parent_category.id
FROM (VALUES
    ('Electrónica', 'Telefonía y wearables'),
    ('Electrónica', 'Computación y audio'),
    ('Hogar y cocina', 'Electrodomésticos'),
    ('Hogar y cocina', 'Hogar y descanso'),
    ('Deportes', 'Entrenamiento'),
    ('Deportes', 'Deportes al aire libre'),
    ('Moda', 'Ropa'),
    ('Moda', 'Accesorios'),
    ('Belleza', 'Cuidado personal'),
    ('Belleza', 'Perfumería'),
    ('Juguetes', 'Construcción y lógica'),
    ('Juguetes', 'Juguetes y peluches')
) AS seed(parent_name, name)
JOIN category parent_category ON parent_category.name = seed.parent_name
    AND parent_category.parent_id IS NULL
WHERE NOT EXISTS (
    SELECT 1 FROM category existing_category
    WHERE existing_category.name = seed.name
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

-- Reubica el catálogo existente en sus subcategorías al actualizar una instalación previa.
UPDATE product existing_product
SET category_id = target_category.id
FROM (VALUES
    ('SKU-001', 'Telefonía y wearables'),
    ('SKU-002', 'Computación y audio'),
    ('SKU-003', 'Telefonía y wearables'),
    ('SKU-004', 'Computación y audio'),
    ('SKU-005', 'Computación y audio'),
    ('SKU-006', 'Electrodomésticos'),
    ('SKU-007', 'Electrodomésticos'),
    ('SKU-008', 'Hogar y descanso'),
    ('SKU-009', 'Hogar y descanso'),
    ('SKU-010', 'Electrodomésticos'),
    ('SKU-011', 'Entrenamiento'),
    ('SKU-012', 'Deportes al aire libre'),
    ('SKU-013', 'Deportes al aire libre'),
    ('SKU-014', 'Deportes al aire libre'),
    ('SKU-015', 'Entrenamiento'),
    ('SKU-016', 'Ropa'),
    ('SKU-017', 'Ropa'),
    ('SKU-018', 'Accesorios'),
    ('SKU-019', 'Accesorios'),
    ('SKU-020', 'Accesorios'),
    ('SKU-021', 'Cuidado personal'),
    ('SKU-022', 'Cuidado personal'),
    ('SKU-023', 'Perfumería'),
    ('SKU-024', 'Cuidado personal'),
    ('SKU-025', 'Cuidado personal'),
    ('SKU-026', 'Construcción y lógica'),
    ('SKU-027', 'Juguetes y peluches'),
    ('SKU-028', 'Construcción y lógica'),
    ('SKU-029', 'Juguetes y peluches'),
    ('SKU-030', 'Juguetes y peluches')
) AS seed(code, category_name)
JOIN category target_category ON target_category.name = seed.category_name
    AND target_category.parent_id IS NOT NULL
WHERE existing_product.code = seed.code;

-- Los precios del catálogo son únicos para asociar cada id generado al producto correcto.
WITH seed(code, name, category_name, price, image_url) AS (
    VALUES
        ('SKU-001', 'Teléfono inteligente Nova X', 'Telefonía y wearables', 499.99, 'https://placehold.co/600x600/png?text=SKU-001'),
        ('SKU-002', 'Auriculares inalámbricos Pulse', 'Computación y audio', 79.99, 'https://placehold.co/600x600/png?text=SKU-002'),
        ('SKU-003', 'Reloj inteligente Active', 'Telefonía y wearables', 129.99, 'https://placehold.co/600x600/png?text=SKU-003'),
        ('SKU-004', 'Laptop portátil Air 14', 'Computación y audio', 899.99, 'https://placehold.co/600x600/png?text=SKU-004'),
        ('SKU-005', 'Altavoz Bluetooth Mini', 'Computación y audio', 59.99, 'https://placehold.co/600x600/png?text=SKU-005'),
        ('SKU-006', 'Cafetera de filtro Classic', 'Electrodomésticos', 49.99, 'https://placehold.co/600x600/png?text=SKU-006'),
        ('SKU-007', 'Licuadora compacta Mix', 'Electrodomésticos', 39.99, 'https://placehold.co/600x600/png?text=SKU-007'),
        ('SKU-008', 'Lámpara de escritorio LED', 'Hogar y descanso', 29.99, 'https://placehold.co/600x600/png?text=SKU-008'),
        ('SKU-009', 'Juego de sábanas Queen', 'Hogar y descanso', 54.99, 'https://placehold.co/600x600/png?text=SKU-009'),
        ('SKU-010', 'Juego de ollas de acero', 'Electrodomésticos', 89.99, 'https://placehold.co/600x600/png?text=SKU-010'),
        ('SKU-011', 'Mat de yoga antideslizante', 'Entrenamiento', 24.99, 'https://placehold.co/600x600/png?text=SKU-011'),
        ('SKU-012', 'Balón de baloncesto Pro', 'Deportes al aire libre', 31.99, 'https://placehold.co/600x600/png?text=SKU-012'),
        ('SKU-013', 'Zapatillas para correr', 'Deportes al aire libre', 74.99, 'https://placehold.co/600x600/png?text=SKU-013'),
        ('SKU-014', 'Casco de ciclismo seguro', 'Deportes al aire libre', 44.99, 'https://placehold.co/600x600/png?text=SKU-014'),
        ('SKU-015', 'Mancuernas ajustables 5 kg', 'Entrenamiento', 42.99, 'https://placehold.co/600x600/png?text=SKU-015'),
        ('SKU-016', 'Camiseta básica de algodón', 'Ropa', 19.99, 'https://placehold.co/600x600/png?text=SKU-016'),
        ('SKU-017', 'Chaqueta urbana ligera', 'Ropa', 99.99, 'https://placehold.co/600x600/png?text=SKU-017'),
        ('SKU-018', 'Mochila clásica diaria', 'Accesorios', 52.99, 'https://placehold.co/600x600/png?text=SKU-018'),
        ('SKU-019', 'Gafas de sol estilo retro', 'Accesorios', 34.99, 'https://placehold.co/600x600/png?text=SKU-019'),
        ('SKU-020', 'Gorra deportiva ajustable', 'Accesorios', 17.99, 'https://placehold.co/600x600/png?text=SKU-020'),
        ('SKU-021', 'Crema hidratante facial', 'Cuidado personal', 22.99, 'https://placehold.co/600x600/png?text=SKU-021'),
        ('SKU-022', 'Champú nutritivo', 'Cuidado personal', 12.99, 'https://placehold.co/600x600/png?text=SKU-022'),
        ('SKU-023', 'Perfume Brisa 50 ml', 'Perfumería', 64.99, 'https://placehold.co/600x600/png?text=SKU-023'),
        ('SKU-024', 'Secador de cabello compacto', 'Cuidado personal', 48.99, 'https://placehold.co/600x600/png?text=SKU-024'),
        ('SKU-025', 'Set de cuidado de la piel', 'Cuidado personal', 41.99, 'https://placehold.co/600x600/png?text=SKU-025'),
        ('SKU-026', 'Bloques de construcción 300 piezas', 'Construcción y lógica', 36.99, 'https://placehold.co/600x600/png?text=SKU-026'),
        ('SKU-027', 'Auto a control remoto', 'Juguetes y peluches', 46.99, 'https://placehold.co/600x600/png?text=SKU-027'),
        ('SKU-028', 'Rompecabezas de 500 piezas', 'Construcción y lógica', 14.99, 'https://placehold.co/600x600/png?text=SKU-028'),
        ('SKU-029', 'Muñeca Aventuras', 'Juguetes y peluches', 27.99, 'https://placehold.co/600x600/png?text=SKU-029'),
        ('SKU-030', 'Peluche de oso clásico', 'Juguetes y peluches', 21.99, 'https://placehold.co/600x600/png?text=SKU-030')
),
missing AS (
    SELECT seed.code, seed.name, seed.category_name,
           seed.price::double precision AS price, seed.image_url
    FROM seed
    WHERE NOT EXISTS (
        SELECT 1 FROM product existing_product WHERE existing_product.code = seed.code
    )
),
new_prices AS (
    INSERT INTO price (price, create_at)
    SELECT missing.price, now()
    FROM missing
    RETURNING id, price
),
new_products AS (
    INSERT INTO product (code, name, create_at, category_id, price_id)
    SELECT missing.code, missing.name, now(), category_row.id, new_prices.id
    FROM missing
    JOIN new_prices ON new_prices.price = missing.price
    JOIN LATERAL (
        SELECT existing_category.id
        FROM category existing_category
        WHERE existing_category.name = missing.category_name
          AND existing_category.parent_id IS NOT NULL
        ORDER BY existing_category.id
        LIMIT 1
    ) category_row ON TRUE
    RETURNING id, code
),
target_products AS (
    SELECT existing_product.id, existing_product.code
    FROM product existing_product
    JOIN seed ON seed.code = existing_product.code
    UNION ALL
    SELECT new_products.id, new_products.code
    FROM new_products
),
image_seeds AS (
    SELECT target_products.id,
           CASE image_number.number
               WHEN 1 THEN seed.image_url
               ELSE seed.image_url || '-' || image_number.number::text
           END AS url
    FROM target_products
    JOIN seed ON seed.code = target_products.code
    CROSS JOIN generate_series(1, 4) AS image_number(number)
)
INSERT INTO image (url, images_id, create_at)
SELECT image_seeds.url, image_seeds.id, now()
FROM image_seeds
WHERE NOT EXISTS (
    SELECT 1
    FROM image existing_image
    WHERE existing_image.images_id = image_seeds.id
      AND existing_image.url = image_seeds.url
);
