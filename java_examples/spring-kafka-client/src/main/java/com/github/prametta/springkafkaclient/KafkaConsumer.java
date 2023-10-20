package com.github.prametta.springkafkaclient;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class KafkaConsumer {
    final static String TOPIC = "quotes";

    @RetryableTopic(kafkaTemplate = "kafkaTemplate", exclude = {}, attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 1.2, maxDelay = 5000))
//    @KafkaListener(id = "listener-1", topics = TOPIC, autoStartup = "true", errorHandler = "validationErrorHandler")
    @KafkaListener(id = "listener-1", topics = TOPIC, autoStartup = "true")
    @SendTo("listener-1.quotes")
    public String consumer(String message, ConsumerRecordMetadata metadata,
                           @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                           @Header(KafkaHeaders.RECEIVED_KEY) String key,
                           @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
                           Acknowledgment acknowledgment) {
        log.info("Key; {}; Partition: {}; Timestamp: {}; Message: {}", key, partition, ts, message);
        log.debug("Offset: {}", metadata.offset());
        acknowledgment.acknowledge();
        return message;
    }

    @Bean
    public KafkaListenerErrorHandler validationErrorHandler() {
        return (m, e) -> {
            log.error("Failed to process message", e);
            return null;
        };
    }
}