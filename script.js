   import http from 'k6/http';
   import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 2000 },
    { duration: '10s', target: 4000 },
    { duration: '10s', target: 8000 },
    { duration: '20s', target: 8000 },
    { duration: '10s', target: 0 },
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