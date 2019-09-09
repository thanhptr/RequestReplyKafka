package com.gauravg.se.callistaenterprise.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;

/**
 * @credit https://callistaenterprise.se/blogg/teknik/2018/10/26/synchronous-request-reply-over-kafka/
 */
public class PartitionAwareReplyingKafkaTemplate<K, V, R> extends ReplyingKafkaTemplate<K, V, R> {

    public PartitionAwareReplyingKafkaTemplate(ProducerFactory<K, V> producerFactory,
                                               GenericMessageListenerContainer<K, R> replyContainer) {
        super(producerFactory, replyContainer);
    }

    private TopicPartition getFirstAssignedReplyTopicPartition() {
        if (getAssignedReplyTopicPartitions() != null &&
                getAssignedReplyTopicPartitions().iterator().hasNext()) {
            org.apache.kafka.common.TopicPartition replyPartition = getAssignedReplyTopicPartitions().iterator().next();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Using partition " + replyPartition.partition());
            }
            return replyPartition;
        } else {
            throw new KafkaException("Illegal state: No reply partition is assigned to this instance");
        }
    }

    private static byte[] intToBytesBigEndian(final int data) {
        return new byte[] {(byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff),};
    }

    @Override
    public RequestReplyFuture<K, V, R> sendAndReceive(ProducerRecord<K, V> record) {
        TopicPartition replyPartition = getFirstAssignedReplyTopicPartition();
        record.headers()
                .add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyPartition.topic().getBytes()))
                .add(new RecordHeader(KafkaHeaders.REPLY_PARTITION,
                        intToBytesBigEndian(replyPartition.partition())));
        return super.sendAndReceive(record);
    }

}