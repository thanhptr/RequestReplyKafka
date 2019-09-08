// k6 run --insecure-skip-tls-verify --summary-trend-stats "min,avg,max,med,p(75),p(90),p(95),p(99),p(99.99)" oe-profile-public.js
import { check } from 'k6';
import http from 'k6/http';

// SIMPLE PEAK
export let options = {
  vus: 6,
  iterations: 6
};

// export let options = {
//   stages: [
//     // // HIGH
//     // { target: 6, duration: "10s" },

//     // // NORMAL
//     // { target: 3, duration: "10s" },
//   ]
// };

export default function() {
    let res = http.get("http://localhost:8181/backend");
    check(res, {
        "OK": r => r.status === 200
    });
};