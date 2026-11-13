CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO _user(dtype,id,create_at,email,firstname,lastname,password,role,username) 
SELECT 'SecurityUser',uuid_generate_v1(),now(),'admin@admin.com','admin','admin','$2a$10$0hcG7w1vstFYfSwJ7G5sjeJomt6x9typiDI0uFJrCJfnl4FrQ6/CK','ADMIN','admin'
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
    SELECT 1 FROM category existing_category WHERE existing_category.name = seed.name
);

-- Los precios del catálogo son únicos para asociar cada id generado al producto correcto.
WITH seed(code, name, category_name, price, image_url) AS (
    VALUES
        ('SKU-001', 'Teléfono inteligente Nova X', 'Electrónica', 499.99, 'https://placehold.co/600x600/png?text=SKU-001'),
        ('SKU-002', 'Auriculares inalámbricos Pulse', 'Electrónica', 79.99, 'https://placehold.co/600x600/png?text=SKU-002'),
        ('SKU-003', 'Reloj inteligente Active', 'Electrónica', 129.99, 'https://placehold.co/600x600/png?text=SKU-003'),
        ('SKU-004', 'Laptop portátil Air 14', 'Electrónica', 899.99, 'https://placehold.co/600x600/png?text=SKU-004'),
        ('SKU-005', 'Altavoz Bluetooth Mini', 'Electrónica', 59.99, 'https://placehold.co/600x600/png?text=SKU-005'),
        ('SKU-006', 'Cafetera de filtro Classic', 'Hogar y cocina', 49.99, 'https://placehold.co/600x600/png?text=SKU-006'),
        ('SKU-007', 'Licuadora compacta Mix', 'Hogar y cocina', 39.99, 'https://placehold.co/600x600/png?text=SKU-007'),
        ('SKU-008', 'Lámpara de escritorio LED', 'Hogar y cocina', 29.99, 'https://placehold.co/600x600/png?text=SKU-008'),
        ('SKU-009', 'Juego de sábanas Queen', 'Hogar y cocina', 54.99, 'https://placehold.co/600x600/png?text=SKU-009'),
        ('SKU-010', 'Juego de ollas de acero', 'Hogar y cocina', 89.99, 'https://placehold.co/600x600/png?text=SKU-010'),
        ('SKU-011', 'Mat de yoga antideslizante', 'Deportes', 24.99, 'https://placehold.co/600x600/png?text=SKU-011'),
        ('SKU-012', 'Balón de baloncesto Pro', 'Deportes', 31.99, 'https://placehold.co/600x600/png?text=SKU-012'),
        ('SKU-013', 'Zapatillas para correr', 'Deportes', 74.99, 'https://placehold.co/600x600/png?text=SKU-013'),
        ('SKU-014', 'Casco de ciclismo seguro', 'Deportes', 44.99, 'https://placehold.co/600x600/png?text=SKU-014'),
        ('SKU-015', 'Mancuernas ajustables 5 kg', 'Deportes', 42.99, 'https://placehold.co/600x600/png?text=SKU-015'),
        ('SKU-016', 'Camiseta básica de algodón', 'Moda', 19.99, 'https://placehold.co/600x600/png?text=SKU-016'),
        ('SKU-017', 'Chaqueta urbana ligera', 'Moda', 99.99, 'https://placehold.co/600x600/png?text=SKU-017'),
        ('SKU-018', 'Mochila clásica diaria', 'Moda', 52.99, 'https://placehold.co/600x600/png?text=SKU-018'),
        ('SKU-019', 'Gafas de sol estilo retro', 'Moda', 34.99, 'https://placehold.co/600x600/png?text=SKU-019'),
        ('SKU-020', 'Gorra deportiva ajustable', 'Moda', 17.99, 'https://placehold.co/600x600/png?text=SKU-020'),
        ('SKU-021', 'Crema hidratante facial', 'Belleza', 22.99, 'https://placehold.co/600x600/png?text=SKU-021'),
        ('SKU-022', 'Champú nutritivo', 'Belleza', 12.99, 'https://placehold.co/600x600/png?text=SKU-022'),
        ('SKU-023', 'Perfume Brisa 50 ml', 'Belleza', 64.99, 'https://placehold.co/600x600/png?text=SKU-023'),
        ('SKU-024', 'Secador de cabello compacto', 'Belleza', 48.99, 'https://placehold.co/600x600/png?text=SKU-024'),
        ('SKU-025', 'Set de cuidado de la piel', 'Belleza', 41.99, 'https://placehold.co/600x600/png?text=SKU-025'),
        ('SKU-026', 'Bloques de construcción 300 piezas', 'Juguetes', 36.99, 'https://placehold.co/600x600/png?text=SKU-026'),
        ('SKU-027', 'Auto a control remoto', 'Juguetes', 46.99, 'https://placehold.co/600x600/png?text=SKU-027'),
        ('SKU-028', 'Rompecabezas de 500 piezas', 'Juguetes', 14.99, 'https://placehold.co/600x600/png?text=SKU-028'),
        ('SKU-029', 'Muñeca Aventuras', 'Juguetes', 27.99, 'https://placehold.co/600x600/png?text=SKU-029'),
        ('SKU-030', 'Peluche de oso clásico', 'Juguetes', 21.99, 'https://placehold.co/600x600/png?text=SKU-030')
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
