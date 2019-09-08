package com.gauravg.controller;

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
public class RequestController {

	private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
	private static final AtomicInteger counter = new AtomicInteger();

	@Autowired
	ReplyingKafkaTemplate<String, Model,Model> kafkaTemplate;

	@Value("${kafka.topic.request-topic}")
	String requestTopic;
	
	@Value("${kafka.topic.requestreply-topic}")
	String requestReplyTopic;

	@GetMapping("/request")
	public ResponseEntity<String> async(@RequestParam(required = false) Integer value) throws InterruptedException, ExecutionException {
		Model request = new Model();
		if (value != null) {
			request.setValue(value);
		}
		request.setRequest(counter.incrementAndGet());

		// create producer record
		ProducerRecord<String, Model> record = new ProducerRecord<String, Model>(requestTopic, request);
		// set reply topic in header
		record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, requestReplyTopic.getBytes()));
		// post in kafka topic
		RequestReplyFuture<String, Model, Model> sendAndReceive = kafkaTemplate.sendAndReceive(record);

		// confirm if producer produced successfully
		SendResult<String, Model> sendResult = sendAndReceive.getSendFuture().get();
		
		//print published all headers
		StringBuilder requestHeaders = new StringBuilder();
		sendResult.getProducerRecord().headers().forEach(header -> requestHeaders.append(header.key()).append(":").append(header.value().toString()).append(" |"));
		logger.info("REQUEST-Started: {} {}", requestHeaders, request);

		// get consumer record
		ConsumerRecord<String, Model> response = sendAndReceive.get();

		logger.info("REQUEST-Completed: {}", response.value());

		// return consumer value
		return new ResponseEntity<>(response.value().toString(), response.value().getStatus() == null || response.value().getStatus() <= 0 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(response.value().getStatus()));
	}
}
