package com.gauravg.controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import com.gauravg.model.Model;

@RestController
public class BackendController {

	private static final Logger logger = LoggerFactory.getLogger(BackendController.class);

	private static final AtomicInteger requestCounter = new AtomicInteger();
	private static final ArrayBlockingQueue<String> SERVER_QUEUE = new ArrayBlockingQueue<String>(3);
	static{
		SERVER_QUEUE.add("S1"); SERVER_QUEUE.add("S2"); SERVER_QUEUE.add("S3");
	}

	@GetMapping("/backend")
	public ResponseEntity<Integer> action(@RequestParam(required = false) Integer value) throws InterruptedException {
		int backendId = requestCounter.incrementAndGet();

		String serverId = SERVER_QUEUE.poll();
		try {
			//Thread.sleep(100);
			Thread.sleep(1000);

			if (serverId == null) {
				logger.info("BACKEND-TOO_MANY_REQUESTS[{}]: {} -> {}", "#", value, backendId);
				return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
			} else {
				long time = System.currentTimeMillis();
				logger.info("BACKEND-OK[{}]: {} -> {}", serverId, value, backendId, time);
				return new ResponseEntity<>(backendId, HttpStatus.OK);
			}
		} finally {
			if (serverId != null) {
				SERVER_QUEUE.put(serverId);
			}
		}
	}
}
