# Redis 기반 캐싱 적용
## 1. 보유 쿠폰 조회 캐시 적용
   1.1. 캐시 적용 개요
   보유 쿠폰 조회 시 반복적인 데이터베이스 접근을 최소화하고 성능을 향상시키기 위해, Redis 기반의 캐싱을 적용하였다.

### 1.2. 캐시 전략 및 설정
캐시 어노테이션 적용:
```java
@Cacheable(value = "userCoupons", key = "'userCoupon:' + #userId", unless = "#result == null")
```
캐시에 값이 없을 경우 자동으로 DB를 조회 후 캐시에 저장됨

결과가 null인 경우는 캐시에 저장하지 않음

TTL 설정:
CacheConfig에서 TTL을 30분으로 설정하여, 일정 시간 후 캐시가 자동 만료되도록 구성함

직렬화/역직렬화 설정:
GenericJackson2JsonRedisSerializer 및 JavaTimeModule을 등록하여 LocalDateTime 등 Java 8 시간 API를 포함한 객체가 JSON 형식으로 Redis에 저장되도록 설정함

캐시 무효화 처리:
결제, 주문 등 쿠폰 상태 변경 시에는 다음 어노테이션을 통해 해당 사용자 캐시를 삭제함
```java
@CacheEvict(value = "userCoupons", key = "'userCoupon:' + #userId")
```
### 1.3. 성능 개선 효과
테스트 시, 사용자에게 10만 건의 쿠폰을 적용하고 쿠폰 조회를 반복 실행

캐시 미적용 시 평균 조회 시간: 약 1500ms

캐시 적용 후 평균 조회 시간: 약 500ms

결과적으로 조회 속도가 약 1/3 수준으로 단축, 성능 개선 효과 확인

## 2. 인기 상품 조회 캐시 적용
   2.1. 캐시 적용 목적
   인기 상품 리스트는 빈번히 노출되며 실시간으로 변동되지 않기 때문에, 일정 주기로 갱신된 데이터를 Redis에 저장하여 항상 캐시가 유지되도록 설계

### 2.2. 캐시 전략
스케줄러 기반 갱신:
인기 상품 조회 캐시 갱신 스케줄러를 매일 00시 30분에 실행되도록 설정

TTL 설정:
캐시 TTL을 25시간으로 지정하여, 인기 상품 목록이 항상 캐시에 존재하도록 유지

DB 조회 빈도 최소화:
애플리케이션 전반에서 인기 상품 조회 시 Redis에 저장된 JSON 데이터를 바로 반환

### 2.3. 관련 코드 요약
```java
@Scheduled(cron = "0 30 0 * * *")
public void updatePopularProductsCache() {
List<BestSellerResult> bestSellers = productService.bestSellerList(...);
redisTemplate.opsForValue().set("bestSellerList", objectMapper.writeValueAsString(bestSellers), 25, TimeUnit.HOURS);
}
```
## 3. 결론 및 기대 효과
   항목	효과
   보유 쿠폰 캐시 적용	응답 속도 약 1/3 단축, DB 부하 감소
   캐시 TTL/무효화 설정	신선한 데이터 유지 및 오염 방지
   인기 상품 캐시 전략	항상 캐시 유지, 조회 속도 안정성 확보


| 항목         | 효과         | 
|--------------|---------------------|
| 보유 쿠폰 캐시 적용	응답 속도 약 1/3 단축    | DB 부하 감소       | 
| 캐시 TTL/무효화 설정  | 신선한 데이터 유지 및 오염 방지                | 
| 인기 상품 캐시 전략	항상 캐시 유지    | 조회 속도 안정성 확보                |

