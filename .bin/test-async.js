// k6 run --insecure-skip-tls-verify --summary-trend-stats "min,avg,max,med,p(75),p(90),p(95),p(99),p(99.99)" oe-profile-public.js
import { check } from 'k6';
import http from 'k6/http';


// SIMPLE PEAK
export let options = {
  vus: 100,
  iterations: 100
};

// export let options = {
//   stages: [
//     // SUPER HIGH
//     { target: 30, duration: "10s" },
//     { target: 100, duration: "10s" },
//     { target: 100, duration: "20s" },
//     { target: 0, duration: "30s" },

//     // // VERY HIGH
//     // { target: 3, duration: "10s" },
//     // { target: 10, duration: "10s" },
//     // { target: 10, duration: "20s" },
//     // { target: 0, duration: "30s" },

//     // // HIGH
//     // { target: 3, duration: "10s" },
//     // { target: 6, duration: "10s" },
//     // { target: 6, duration: "20s" },
//     // { target: 0, duration: "20s" },

//     // // NORMAL
//     // { target: 3, duration: "10s" },
//     // { target: 0, duration: "10s" },
//   ]
// };

export default function() {
    let res = http.get("http://localhost:8181/request?value=1");
    check(res, {
        "OK": r => r.status === 200
    });
};