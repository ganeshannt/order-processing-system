-- src/main/resources/data-dev.sql
-- Development data initialization script

-- Order 1: PENDING
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (1, 'PENDING', 'john.doe@example.com', 2549.97, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (1, 'MacBook Pro 16', 1, 2499.99, 1);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (2, 'USB-C Cable', 2, 24.99, 1);

-- Order 2: PROCESSING
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (2, 'PROCESSING', 'jane.smith@example.com', 1899.98,
        DATEADD('HOUR', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (3, 'Dell XPS 15 Laptop', 1, 1799.99, 2);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (4, 'Laptop Bag', 1, 99.99, 2);

-- Order 3: SHIPPED
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (3, 'SHIPPED', 'bob.johnson@example.com', 3247.95,
        DATEADD('DAY', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (5, 'iPhone 15 Pro', 1, 1199.99, 3);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (6, 'AirPods Pro', 1, 249.99, 3);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (7, 'iPhone Case', 2, 49.99, 3);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (8, 'Screen Protector', 3, 29.99, 3);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (9, 'Lightning Cable', 5, 19.99, 3);

-- Order 4: DELIVERED
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (4, 'DELIVERED', 'alice.williams@example.com', 899.97,
        DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP));

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (10, 'Samsung Galaxy Tab', 1, 699.99, 4);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (11, 'Tablet Stand', 1, 49.99, 4);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (12, 'Stylus Pen', 1, 149.99, 4);

-- Order 5: CANCELLED
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (5, 'CANCELLED', 'charlie.brown@example.com', 1599.99,
        DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP));

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (13, 'Gaming Console', 1, 499.99, 5);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (14, 'Extra Controller', 2, 69.99, 5);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (15, 'Game Title: Adventure Quest', 5, 59.99, 5);

-- Order 6: Another PENDING
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (6, 'PENDING', 'david.lee@example.com', 4299.94, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (16, '4K Monitor 32', 2, 799.99, 6);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (17, 'Monitor Arm', 2, 149.99, 6);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (18, 'HDMI Cable', 4, 24.99, 6);

-- Order 7: PENDING
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (7, 'PENDING', 'emma.davis@example.com', 2199.95, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (19, 'Sony WH-1000XM5 Headphones', 1, 399.99, 7);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (20, 'Portable SSD 2TB', 1, 299.99, 7);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (21, 'Mechanical Keyboard', 1, 149.99, 7);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (22, 'Gaming Mouse', 1, 79.99, 7);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (23, 'Mouse Pad', 5, 19.99, 7);

-- Order 8: PROCESSING
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (8, 'PROCESSING', 'frank.miller@example.com', 649.92,
        DATEADD('HOUR', -3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (24, 'Webcam HD', 1, 199.99, 8);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (25, 'Ring Light', 1, 89.99, 8);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (26, 'Microphone', 1, 149.99, 8);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (27, 'Boom Arm', 1, 49.99, 8);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (28, 'Pop Filter', 2, 19.99, 8);

-- Order 9: Same customer as Order 1
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (9, 'DELIVERED', 'john.doe@example.com', 1249.98,
        DATEADD('DAY', -30, CURRENT_TIMESTAMP), DATEADD('DAY', -25, CURRENT_TIMESTAMP));

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (29, 'Smart Watch', 1, 449.99, 9);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (30, 'Watch Band', 2, 49.99, 9);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (31, 'Screen Protector', 5, 14.99, 9);

-- Order 10: Large order
INSERT INTO orders (id, status, customer_email, total_amount, created_at, updated_at)
VALUES (10, 'PENDING', 'grace.taylor@example.com', 5749.85, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (32, 'Desktop PC', 1, 2999.99, 10);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (33, '4K Monitor 27"', 2, 599.99, 10);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (34, 'Mechanical Keyboard RGB', 1, 179.99, 10);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (35, 'Gaming Mouse Pro', 1, 129.99, 10);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (36, 'Desk Mat XXL', 1, 39.99, 10);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (37, 'USB Hub', 2, 49.99, 10);

INSERT INTO order_items (id, product_name, quantity, price, order_id)
VALUES (38, 'Cable Management Kit', 3, 19.99, 10);

-- CRITICAL: Reset sequences at the end
ALTER TABLE orders
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM orders);
ALTER TABLE order_items
    ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM order_items);
