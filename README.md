# Environment
- Maven build: http://localhost:8181
- KAFKA Server: localhost:9092

# Service
- Run a full flow of request from REQUEST -> KAFKA -> REPLY -> BACKEND
- URL: http://localhost:8181/request?value={int-value}
    - Request value (Integer): the input value
    - Response: the object contains full process ids
```javascript
Model {
    value: 1,       // Input value
    request: 10,    // Requestor API - incremental value
    reply: 10,      // KAFKA Reply Executor - incremental value
    backend: 10,    // Backend API - incremental value
    status: 200     // HTTP-Status code (200 - OK)
}
```

# Backend
- Limit to 3 CCUs, 1 second/request ~ 3 TPS
- Users over 3 CCUs will generate TOO_MANY_REQUESTS error
- URL: http://localhost:8181/backend?value={int-value}
    - Request value (Integer): the KAFKA Replying Request ID
    - Response: the incremential integer value

# Load Test
## Direct Backend
```javascript
import { check } from 'k6';
import http from 'k6/http';

// SIMPLE PEAK - 6 CCUs (expect 3 OK, 3 ERR)
export let options = {
    vus: 6,
    iterations: 6
};

export default function() {
    let res = http.get("http://localhost:8181/backend?value=1");
    check(res, {
        "OK": r => r.status === 200
    });
};
```
```bash
$ k6 run .bin/test-sync.js

          /\      |‾‾|  /‾‾/  /‾/
     /\  /  \     |  |_/  /  / /
    /  \/    \    |      |  /  ‾‾\
   /          \   |  |‾\  \ | (_) |
  / __________ \  |__|  \__\ \___/ .io

  execution: local
     output: -
     script: .bin/test-sync.js

    duration: -,  iterations: 6
         vus: 6, max: 6

    done [==========================================================] 6 / 6

    ✗ OK
     ↳  50% — ✓ 3 / ✗ 3

    checks.....................: 50.00% ✓ 3   ✗ 3
    data_received..............: 637 B  630 B/s
    data_sent..................: 522 B  516 B/s
    http_req_blocked...........: avg=1.11ms   min=569.1µs med=833.6µs  max=2.6ms  p(90)=1.88ms  p(95)=2.24ms
    http_req_connecting........: avg=930.95µs min=436.2µs med=602.3µs  max=2.39ms p(90)=1.71ms  p(95)=2.05ms
    http_req_duration..........: avg=1s       min=1s      med=1s       max=1s     p(90)=1s      p(95)=1s
    http_req_receiving.........: avg=32.33µs  min=16.1µs  med=21.24µs  max=61.3µs p(90)=57.99µs p(95)=59.65µs
    http_req_sending...........: avg=619.3µs  min=149.2µs med=184.45µs max=2.76ms p(90)=1.52ms  p(95)=2.14ms
    http_req_tls_handshaking...: avg=0s       min=0s      med=0s       max=0s     p(90)=0s      p(95)=0s
    http_req_waiting...........: avg=1s       min=1s      med=1s       max=1s     p(90)=1s      p(95)=1s
    http_reqs..................: 6      5.937937/s
    iteration_duration.........: avg=1s       min=1s      med=1s       max=1s     p(90)=1s      p(95)=1s
    iterations.................: 6      5.937937/s
    vus........................: 6      min=6 max=6
    vus_max....................: 6      min=6 max=6
```

## Through KAFKA
```javascript
import { check } from 'k6';
import http from 'k6/http';

// SIMPLE PEAK - 100 CCUs (expect 100% success, running in 34 seconds)
export let options = {
    vus: 100,
    iterations: 100
};

export default function() {
    let res = http.get("http://localhost:8181/request?value=1");
    check(res, {
        "OK": r => r.status === 200
    });
};
```
```bash
$ k6 run .bin/test-async.js

          /\      |‾‾|  /‾‾/  /‾/
     /\  /  \     |  |_/  /  / /
    /  \/    \    |      |  /  ‾‾\
   /          \   |  |‾\  \ | (_) |
  / __________ \  |__|  \__\ \___/ .io

  execution: local
     output: -
     script: .bin/test-async.js

    duration: -,    iterations: 100
         vus: 100, max: 100

    done [==========================================================] 100 / 100

    ✗ OK
     ↳  4% — ✓ 4 / ✗ 96

    checks.....................: 4.00%  ✓ 4     ✗ 96
    data_received..............: 33 kB  6.3 kB/s
    data_sent..................: 9.5 kB 1.8 kB/s
    http_req_blocked...........: avg=22.32ms  min=351.4µs med=24.91ms max=51.27ms p(90)=44.67ms p(95)=49.07ms
    http_req_connecting........: avg=22.13ms  min=233µs   med=24.01ms max=51.1ms  p(90)=44.24ms p(95)=49ms
    http_req_duration..........: avg=5.02s    min=1.07s   med=5.13s   max=5.16s   p(90)=5.14s   p(95)=5.15s
    http_req_receiving.........: avg=232.62µs min=10.5µs  med=14.95µs max=17.45ms p(90)=42.53µs p(95)=140.78µs
    http_req_sending...........: avg=3.57ms   min=50.3µs  med=431.5µs max=18.51ms p(90)=11.06ms p(95)=18.31ms
    http_req_tls_handshaking...: avg=0s       min=0s      med=0s      max=0s      p(90)=0s      p(95)=0s
    http_req_waiting...........: avg=5.01s    min=1.07s   med=5.13s   max=5.15s   p(90)=5.14s   p(95)=5.14s
    http_reqs..................: 100    19.22359/s
    iteration_duration.........: avg=5.05s    min=1.1s    med=5.14s   max=5.2s    p(90)=5.19s   p(95)=5.2s
    iterations.................: 100    19.22359/s
    vus........................: 100    min=100 max=100
    vus_max....................: 100    min=100 max=100
```