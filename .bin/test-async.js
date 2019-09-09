// k6 run --insecure-skip-tls-verify --summary-trend-stats "min,avg,max,med,p(75),p(90),p(95),p(99),p(99.99)" oe-profile-public.js
import { check } from 'k6';
import http from 'k6/http';

export let options = {
  // vus: 100,
  // iterations: 100 * 10
  vus: 10,
  iterations: 10
};

export default function() {
  let params = {
    timeout: 10 * 60 * 1000
  };  
  let res = http.get("http://localhost:2001/request?value=1", params);
  check(res, {
      "OK": r => r.status === 200
  });
};