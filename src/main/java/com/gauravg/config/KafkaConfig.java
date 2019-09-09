package com.gauravg.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.gauravg.model.Model;

@Configuration
public class KafkaConfig {

	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${kafka.topic.requestreply-topic}")
	private String requestReplyTopic;

	@Value("${kafka.consumergroup}")
	private String consumerGroup;

	@Value("${kafka.security.protocol}")
	private String securityProtocol;

	@Value("${kafka.ssl-truststore-location}")
	private String sslTruststoreLocation;

	@Value("${kafka.ssl-truststore-password}")
	private String sslTruststorePassword;

	@Value("${kafka.ssl-key-password}")
	private String sslKeyPassword;

	@Value("${kafka.ssl-keystore-password}")
	private String sslKeystorePassword;

	@Value("${kafka.ssl-keystore-location}")
	private String sslKeystoreLocation;

	private Map<String, Object> loadSslConfigs(Map<String, Object> props) {
		if (securityProtocol != null && securityProtocol.equals("SSL")) {
			props.put("security.protocol", securityProtocol);
			props.put("ssl.truststore.location", sslTruststoreLocation);
			props.put("ssl.truststore.password", sslTruststorePassword);

			props.put("ssl.key.password", sslKeyPassword);
			props.put("ssl.keystore.password", sslKeystorePassword);
			props.put("ssl.keystore.location", sslKeystoreLocation);
		}
		return props;
	}

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		// list of host:port pairs used for establishing the initial connections to the Kakfa cluster
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return loadSslConfigs(props);
	}

	@Bean
	public Map<String, Object> consumerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
		return loadSslConfigs(props);
	}

	@Bean
	public ProducerFactory<String,Model> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public KafkaTemplate<String, Model> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public ReplyingKafkaTemplate<String, Model, Model> replyKafkaTemplate(ProducerFactory<String, Model> pf, KafkaMessageListenerContainer<String, Model> container){
		ReplyingKafkaTemplate<String, Model, Model> replyTemplate = new ReplyingKafkaTemplate<>(pf, container);
		replyTemplate.setReplyTimeout(10 * 60000);
		return replyTemplate;
	}

	@Bean
	public KafkaMessageListenerContainer<String, Model> replyContainer(ConsumerFactory<String, Model> cf) {
		ContainerProperties containerProperties = new ContainerProperties(requestReplyTopic);
		return new KafkaMessageListenerContainer<>(cf, containerProperties);
	}

	@Bean
	public ConsumerFactory<String, Model> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(),new StringDeserializer(),new JsonDeserializer<>(Model.class));
	}

	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Model>> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Model> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setReplyTemplate(kafkaTemplate());
		return factory;
	}
}