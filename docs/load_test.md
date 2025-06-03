# 부하 테스트 계획서
## 1. 테스트 대상 선정
   대상 API: /api/v1/coupons/issue

대상 기능: 선착순 쿠폰 발급 기능

선정 이유: 해당 기능은 이벤트성으로 대량의 사용자가 동시에 접근하는 구간이 예상되며, 시스템의 안정성 및 동시 처리 성능을 점검할 필요가 있음.

목표: 대규모 동시 요청에 대한 서버의 안정성, 응답 시간, 오류율 등을 파악하여 병목지점 식별 및 성능 개선 방향 도출

## 2. 테스트 목적
   선착순 쿠폰 발급 시 동시성 제어 및 트래픽 급증 상황에 대한 내구성 확인

시스템의 최대 처리 가능 트래픽 추정 및 병목 파악

성공률(HTTP 2xx), 실패율(5xx, timeout 등), 응답 시간 분포 측정

## 3. 테스트 시나리오
   사용자 수: 최대 10,000명 중 1,000명 가상 사용자(Virtual Users)

요청 유형: POST 요청 (쿠폰 발급 시도)

요청 간격: 사용자당 1초 간격 대기

총 실행 시간: 10초 (단계적 증가 테스트 시에는 점진적 확대 방식 적용)

## 4. 점진적 부하 증가 전략 (Ramp-Up 방식)
   초기부터 최대 부하를 주는 대신, 아래와 같이 단계별로 가상 사용자를 늘려가며 테스트를 진행

```javascript
export const options = {
stages: [
{ duration: '5s', target: 200 },   // 0~5초: 200명까지 증가
{ duration: '5s', target: 500 },   // 5~10초: 500명까지 증가
{ duration: '5s', target: 1000 },  // 10~15초: 최대 1000명 도달
{ duration: '10s', target: 1000 }, // 15~25초: 부하 유지
{ duration: '5s', target: 0 },     // 25~30초: 부하 감소 (정리)
],
};
```
## 5. 테스트 스크립트 (k6)
   
```javascript
   import http from 'k6/http';
   import { sleep, check } from 'k6';

export const options = {
stages: [
{ duration: '5s', target: 200 },
{ duration: '5s', target: 500 },
{ duration: '5s', target: 1000 },
{ duration: '10s', target: 1000 },
{ duration: '5s', target: 0 },
],
};

export default function () {
const url = 'http://localhost:8080/api/v1/coupons/issue';

const userId = Math.floor(Math.random() * 10000) + 1;
const couponId = 1;

const payload = JSON.stringify({
userId: userId,
couponId: couponId,
});

const params = {
headers: {
'Content-Type': 'application/json',
},
};

const res = http.post(url, payload, params);

check(res, {
'status is 2xx': (r) => r.status >= 200 && r.status < 300,
});

sleep(1);
}
```

## 6. 기대 결과 및 확인 포인트
   응답 성공률이 95% 이상 유지되는가?

응답 시간의 p95, p99 지표가 비정상적으로 높지 않은가?

쿠폰 중복 발급이나 재고 초과 등의 예외 상황은 올바르게 처리되고 있는가?

Redis, Kafka 등의 부하도 모니터링 대상에 포함하여 병목 지점을 파악

