SET SESSION cte_max_recursion_depth = 10000;

INSERT INTO order_product (amount, created_at, orders_id, product_id, quantity, updated_at)
WITH RECURSIVE cte (n) AS (
  SELECT 1
  UNION ALL
  SELECT n + 1 FROM cte WHERE n < 10000
)
SELECT
  1000,                             -- amount
  NOW() - INTERVAL 2 DAY,           -- created_at (5일 전으로 세팅)
  FLOOR(1 + (RAND() * 1000)),       -- orders_id
  FLOOR(1 + (RAND() * 100)),        -- product_id
  FLOOR(1 + (RAND() * 5)),          -- quantity
  NOW()                             -- updated_at
FROM cte;

CREATE INDEX idx_order_product_created_at_product_id
ON order_product (created_at, product_id);

DROP INDEX idx_order_product_created_at_product_id ON order_product;