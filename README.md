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

// SIMPLE PEAK - 10 CCUs (expect 100% success, running in 4 seconds)
export let options = {
    vus: 10,
    iterations: 10
};

export default function() {
  let params = {
    timeout: 10 * 60 * 1000
  };  
  let res = http.get("http://localhost:8181/request?value=1", params);
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

    duration: -,   iterations: 10
         vus: 10, max: 10

    done [==========================================================] 10 / 10

    ✗ OK
     ↳  40% — ✓ 4 / ✗ 6

    checks.....................: 40.00% ✓ 4    ✗ 6
    data_received..............: 2.7 kB 508 B/s
    data_sent..................: 950 B  179 B/s
    http_req_blocked...........: avg=1.8ms    min=519.9µs med=1.81ms  max=3.19ms  p(90)=3.05ms   p(95)=3.12ms
    http_req_connecting........: avg=1.7ms    min=411.2µs med=1.68ms  max=3.12ms  p(90)=2.95ms   p(95)=3.03ms
    http_req_duration..........: avg=4.33s    min=1.36s   med=5.29s   max=5.3s    p(90)=5.3s     p(95)=5.3s
    http_req_receiving.........: avg=40.62µs  min=13.2µs  med=38.55µs max=81µs    p(90)=76.23µs  p(95)=78.61µs
    http_req_sending...........: avg=247.45µs min=65.8µs  med=140.2µs max=962.7µs p(90)=447.89µs p(95)=705.29µs
    http_req_tls_handshaking...: avg=0s       min=0s      med=0s      max=0s      p(90)=0s       p(95)=0s
    http_req_waiting...........: avg=4.33s    min=1.36s   med=5.29s   max=5.3s    p(90)=5.3s     p(95)=5.3s
    http_reqs..................: 10     1.886273/s
    iteration_duration.........: avg=4.33s    min=1.36s   med=5.3s    max=5.3s    p(90)=5.3s     p(95)=5.3s
    iterations.................: 10     1.886273/s
    vus........................: 10     min=10 max=10
    vus_max....................: 10     min=10 max=10
```
```
ERROR 2516 --- [nio-8181-exec-4] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.util.concurrent.ExecutionException: org.springframework.kafka.KafkaException: Reply timed out] with root cause

org.springframework.kafka.KafkaException: Reply timed out
	at org.springframework.kafka.requestreply.ReplyingKafkaTemplate.lambda$sendAndReceive$0(ReplyingKafkaTemplate.java:196) ~[spring-kafka-2.1.5.RELEASE.jar:2.1.5.RELEASE]
	at org.springframework.scheduling.support.DelegatingErrorHandlingRunnable.run(DelegatingErrorHandlingRunnable.java:54) ~[spring-context-5.0.5.RELEASE.jar:5.0.5.RELEASE]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) ~[na:1.8.0_162]
	at java.util.concurrent.FutureTask.run(FutureTask.java:266) ~[na:1.8.0_162]
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$201(ScheduledThreadPoolExecutor.java:180) ~[na:1.8.0_162]
	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293) ~[na:1.8.0_162]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149) ~[na:1.8.0_162]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624) ~[na:1.8.0_162]
	at java.lang.Thread.run(Thread.java:748) [na:1.8.0_162]
```
```
REQUEST-Started	kafka_replyTopic:[B@649bd5f9 |kafka_correlationId:[B@16f6a974 |__TypeId__:[B@2a242ed6 | Model{value=1, request=10, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@29544657 |kafka_correlationId:[B@79ad0d1f |__TypeId__:[B@7c7a00a3 | Model{value=1, request=2, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@70a312e8 |kafka_correlationId:[B@63411a1f |__TypeId__:[B@32dc2104 | Model{value=1, request=3, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@749a852b |kafka_correlationId:[B@62fb9743 |__TypeId__:[B@76b885c4 | Model{value=1, request=5, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@7fc69388 |kafka_correlationId:[B@638c4fa1 |__TypeId__:[B@4927e611 | Model{value=1, request=1, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@a1c24c |kafka_correlationId:[B@27b035e6 |__TypeId__:[B@4e4a1add | Model{value=1, request=8, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@3de2992e |kafka_correlationId:[B@7050dc9a |__TypeId__:[B@31de74c5 | Model{value=1, request=9, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@4b369bbb |kafka_correlationId:[B@75c51b6 |__TypeId__:[B@24223096 | Model{value=1, request=4, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@1eb7ea2c |kafka_correlationId:[B@422ee7d7 |__TypeId__:[B@186028e2 | Model{value=1, request=6, reply=null, backend=null, status=null}
REQUEST-Started	kafka_replyTopic:[B@1255a525 |kafka_correlationId:[B@6023c5da |__TypeId__:[B@20a0d0e4 | Model{value=1, request=7, reply=null, backend=null, status=null}
BACKEND-OK[S1]	1 -> 1
REPLY-OK	Model{value=1, request=3, reply=1, backend=1, status=200}
REQUEST-Completed	Model{value=1, request=3, reply=1, backend=1, status=200}
BACKEND-OK[S2]	2 -> 2
REPLY-OK	Model{value=1, request=2, reply=2, backend=2, status=200}
REQUEST-Completed	Model{value=1, request=2, reply=2, backend=2, status=200}
BACKEND-OK[S3]	3 -> 3
REPLY-OK	Model{value=1, request=7, reply=3, backend=3, status=200}
REQUEST-Completed	Model{value=1, request=7, reply=3, backend=3, status=200}
BACKEND-OK[S1]	4 -> 4
REPLY-OK	Model{value=1, request=5, reply=4, backend=4, status=200}
REQUEST-Completed	Model{value=1, request=5, reply=4, backend=4, status=200}
Reply timed out for	ProducerRecord(topic=request-topic, partition=null, headers=RecordHeaders(headers = [RecordHeader(key = kafka_replyTopic, value = [114, 101, 113, 117, 101, 115, 116, 114, 101, 112, 108, 121, 45, 116, 111, 112, 105, 99]), RecordHeader(key = kafka_correlationId, value = [-9, -59, 3, 84, -53, -75, 79, 101, -122, -108, -31, 24, 1, -107, 30, -43]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = true), key=null, value=Model{value=1, request=10, reply=null, backend=null, status=null}, timestamp=null) with correlationId	[-10940101915493772153243907696974553387]
Reply timed out for	ProducerRecord(topic=request-topic, partition=null, headers=RecordHeaders(headers = [RecordHeader(key = kafka_replyTopic, value = [114, 101, 113, 117, 101, 115, 116, 114, 101, 112, 108, 121, 45, 116, 111, 112, 105, 99]), RecordHeader(key = kafka_correlationId, value = [17, 79, -14, -15, -98, 6, 74, 49, -80, 16, -110, -78, -18, 74, -23, 20]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = true), key=null, value=Model{value=1, request=9, reply=null, backend=null, status=null}, timestamp=null) with correlationId	[23011994866185306342353786560257386772]
Reply timed out for	ProducerRecord(topic=request-topic, partition=null, headers=RecordHeaders(headers = [RecordHeader(key = kafka_replyTopic, value = [114, 101, 113, 117, 101, 115, 116, 114, 101, 112, 108, 121, 45, 116, 111, 112, 105, 99]), RecordHeader(key = kafka_correlationId, value = [-15, 8, 66, -48, -34, 76, 70, 78, -92, 12, -75, 126, 116, -43, 48, 68]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = true), key=null, value=Model{value=1, request=1, reply=null, backend=null, status=null}, timestamp=null) with correlationId	[-19895526374615932936348483582484467644]
Reply timed out for	ProducerRecord(topic=request-topic, partition=null, headers=RecordHeaders(headers = [RecordHeader(key = kafka_replyTopic, value = [114, 101, 113, 117, 101, 115, 116, 114, 101, 112, 108, 121, 45, 116, 111, 112, 105, 99]), RecordHeader(key = kafka_correlationId, value = [-90, 111, 47, -105, 44, 26, 79, -8, -120, -54, 11, 25, 114, 28, -63, -53]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = true), key=null, value=Model{value=1, request=8, reply=null, backend=null, status=null}, timestamp=null) with correlationId	[-119053209418992001257100304883996900917]
Reply timed out for	ProducerRecord(topic=request-topic, partition=null, headers=RecordHeaders(headers = [RecordHeader(key = kafka_replyTopic, value = [114, 101, 113, 117, 101, 115, 116, 114, 101, 112, 108, 121, 45, 116, 111, 112, 105, 99]), RecordHeader(key = kafka_correlationId, value = [-62, 24, 111, -38, -63, 105, 65, 33, -74, -110, 116, -84, -118, -109, -61, 17]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = true), key=null, value=Model{value=1, request=6, reply=null, backend=null, status=null}, timestamp=null) with correlationId	[-82285251934996663129609871255514397935]
Reply timed out for	ProducerRecord(topic=request-topic, partition=null, headers=RecordHeaders(headers = [RecordHeader(key = kafka_replyTopic, value = [114, 101, 113, 117, 101, 115, 116, 114, 101, 112, 108, 121, 45, 116, 111, 112, 105, 99]), RecordHeader(key = kafka_correlationId, value = [-59, 24, -34, -88, -50, -79, 79, -68, -76, -19, -122, -37, -20, 34, 119, -73]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = true), key=null, value=Model{value=1, request=4, reply=null, backend=null, status=null}, timestamp=null) with correlationId	[-78295320557473619129750921783449323593]
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.util.concurrent.ExecutionException	org.springframework.kafka.KafkaException	Reply timed out] with root cause
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.util.concurrent.ExecutionException	org.springframework.kafka.KafkaException	Reply timed out] with root cause
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.util.concurrent.ExecutionException	org.springframework.kafka.KafkaException	Reply timed out] with root cause
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.util.concurrent.ExecutionException	org.springframework.kafka.KafkaException	Reply timed out] with root cause
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.util.concurrent.ExecutionException	org.springframework.kafka.KafkaException	Reply timed out] with root cause
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.util.concurrent.ExecutionException	org.springframework.kafka.KafkaException	Reply timed out] with root cause
BACKEND-OK[S2]	5 -> 5
REPLY-OK	Model{value=1, request=9, reply=5, backend=5, status=200}
No pending reply	ConsumerRecord(topic = requestreply-topic, partition = 0, offset = 63194, CreateTime = 1567956256272, serialized key size = -1, serialized value size = 58, headers = RecordHeaders(headers = [RecordHeader(key = kafka_correlationId, value = [17, 79, -14, -15, -98, 6, 74, 49, -80, 16, -110, -78, -18, 74, -23, 20]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = false), key = null, value = Model{value=1, request=9, reply=5, backend=5, status=200}) with correlationId	[23011994866185306342353786560257386772], perhaps timed out
BACKEND-OK[S3]	6 -> 6
REPLY-OK	Model{value=1, request=10, reply=6, backend=6, status=200}
No pending reply	ConsumerRecord(topic = requestreply-topic, partition = 0, offset = 63195, CreateTime = 1567956257282, serialized key size = -1, serialized value size = 59, headers = RecordHeaders(headers = [RecordHeader(key = kafka_correlationId, value = [-9, -59, 3, 84, -53, -75, 79, 101, -122, -108, -31, 24, 1, -107, 30, -43]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = false), key = null, value = Model{value=1, request=10, reply=6, backend=6, status=200}) with correlationId	[-10940101915493772153243907696974553387], perhaps timed out
BACKEND-OK[S1]	7 -> 7
REPLY-OK	Model{value=1, request=6, reply=7, backend=7, status=200}
No pending reply	ConsumerRecord(topic = requestreply-topic, partition = 0, offset = 63196, CreateTime = 1567956258292, serialized key size = -1, serialized value size = 58, headers = RecordHeaders(headers = [RecordHeader(key = kafka_correlationId, value = [-62, 24, 111, -38, -63, 105, 65, 33, -74, -110, 116, -84, -118, -109, -61, 17]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = false), key = null, value = Model{value=1, request=6, reply=7, backend=7, status=200}) with correlationId	[-82285251934996663129609871255514397935], perhaps timed out
BACKEND-OK[S2]	8 -> 8
REPLY-OK	Model{value=1, request=8, reply=8, backend=8, status=200}
No pending reply	ConsumerRecord(topic = requestreply-topic, partition = 0, offset = 63197, CreateTime = 1567956259299, serialized key size = -1, serialized value size = 58, headers = RecordHeaders(headers = [RecordHeader(key = kafka_correlationId, value = [-90, 111, 47, -105, 44, 26, 79, -8, -120, -54, 11, 25, 114, 28, -63, -53]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = false), key = null, value = Model{value=1, request=8, reply=8, backend=8, status=200}) with correlationId	[-119053209418992001257100304883996900917], perhaps timed out
BACKEND-OK[S3]	9 -> 9
REPLY-OK	Model{value=1, request=1, reply=9, backend=9, status=200}
No pending reply	ConsumerRecord(topic = requestreply-topic, partition = 0, offset = 63198, CreateTime = 1567956260304, serialized key size = -1, serialized value size = 58, headers = RecordHeaders(headers = [RecordHeader(key = kafka_correlationId, value = [-15, 8, 66, -48, -34, 76, 70, 78, -92, 12, -75, 126, 116, -43, 48, 68]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = false), key = null, value = Model{value=1, request=1, reply=9, backend=9, status=200}) with correlationId	[-19895526374615932936348483582484467644], perhaps timed out
BACKEND-OK[S1]	10 -> 10
REPLY-OK	Model{value=1, request=4, reply=10, backend=10, status=200}
No pending reply	ConsumerRecord(topic = requestreply-topic, partition = 0, offset = 63199, CreateTime = 1567956261311, serialized key size = -1, serialized value size = 60, headers = RecordHeaders(headers = [RecordHeader(key = kafka_correlationId, value = [-59, 24, -34, -88, -50, -79, 79, -68, -76, -19, -122, -37, -20, 34, 119, -73]), RecordHeader(key = __TypeId__, value = [99, 111, 109, 46, 103, 97, 117, 114, 97, 118, 103, 46, 109, 111, 100, 101, 108, 46, 77, 111, 100, 101, 108])], isReadOnly = false), key = null, value = Model{value=1, request=4, reply=10, backend=10, status=200}) with correlationId	[-78295320557473619129750921783449323593], perhaps timed out
```