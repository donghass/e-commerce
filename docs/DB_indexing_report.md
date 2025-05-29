# DB 인덱스 사용

 인덱스 사용 시 주의할 점
- 인덱스는 저장을 위해 별도의 공간이 필요함 ( 너무 많아도 문제 )
- 인덱스 조정을 위해 데이터의 삽입/수정/삭제 연산 성능이 하락

인덱스 필요 시점

- 데이터가 너무 많아 조건에 맞게 조회하는 데에 속도가 오래 걸리는 경우



## 성능 개선 AIP
최근 3일 기간 주문 내역 조회

### 1. 테스트 목적

   최근 3일 주문 내역을 조회하는 쿼리에 대해
   PK가 아닌 컬럼 기반 조회는 데이터가 많아질수록 병목 가능성이 높다고 판단.
   이에 따라 인덱스 전후 성능을 비교합니다.
PK가 아닌 값으로 조회하는 쿼리들이 데이터가 많아지면 병목 쿼리가 될 수 있는 가능성이 높다고 생각하여 아래 케이스의 인덱스 전과 후를 비교하고자 합니다.

order_product 1,000,000 건, 3일 이내 order_product 100 건

### 2. 테스트 쿼리
SELECT
p.id AS productId,
p.name,
p.price,
p.stock,
SUM(op.quantity) AS sales,
now(),
now()
FROM order_product op
JOIN product p ON op.product_id = p.id
WHERE op.created_at >= NOW() - INTERVAL 3 DAY
GROUP BY p.id
ORDER BY sales DESC

### 3. 인덱스 적용 전 실행계획
### 4. 인덱스 적용 후 실행계획

### 5. 성능분석
   지표	            인덱스 적용 전	   인덱스 적용 후
   실행 시간	        164ms	           3ms
   전체비용	        456,599	           155
   데이터 검색 방식	전체 테이블 스캔	   인덱스 검색
   처리 행 수	    1,000,000건        101건

### 6. 인덱스 적용 
   CREATE INDEX idx_order_product_created_at_product_id
   ON order_product (created_at, product_id);
