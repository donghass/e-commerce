import http from 'k6/http';
import { sleep } from 'k6';
import { check } from 'k6';

export const options = {
  vus: 1000,         // 가상 사용자 1만명
  duration: '10s',    // 30초 동안 유지
};

export default function () {
  const url = 'http://localhost:8080/api/v1/coupons/issue';

  const userId = Math.floor(Math.random() * 10000) + 1;  // 1~10,000 랜덤 사용자
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

  // 성공 여부 확인 (HTTP 2xx만 성공 처리)
  check(res, {
    'status is 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  sleep(1); // 사용자당 1초 대기
}
