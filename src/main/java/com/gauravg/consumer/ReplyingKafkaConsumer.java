package com.gauravg.consumer;

import com.gauravg.controller.RequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.gauravg.model.Model;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;


@Component
public class ReplyingKafkaConsumer {

	private static final Logger logger = LoggerFactory.getLogger(ReplyingKafkaConsumer.class);
	private static final AtomicInteger counter = new AtomicInteger();

	@Value("${server.nginx}")
	private String serverNginx;

	@Value("${server.port}")
	private String serverPort;

	@KafkaListener(topics = "${kafka.topic.request-topic}")
	@SendTo
	public Model listen(Model request) throws InterruptedException {

		final int replyId = counter.incrementAndGet();
		request.setReply(replyId);

		final String uri = "http://localhost:" + (serverNginx == "1" ? "2000" : serverPort) + "/backend?value=" + replyId;

		RestTemplate restTemplate = new RestTemplate();
		try {
			request.setBackend(restTemplate.getForObject(uri, Integer.class));
			request.setStatus(HttpStatus.OK.value());
			logger.info("REPLY-OK: {}", request);
		} catch (RestClientException ex) {
			request.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			logger.info("REPLY-FAILED: {} -> {}", request, ex.getMessage());
		}

		return request;
	}
}
