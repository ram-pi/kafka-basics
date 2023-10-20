package com.github.prametta.springkafkaclient;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
public class KafkaTransactionalClient {

    final static String TOPIC = "quotes";
    final static String TRANSACTIONAL_TOPIC = "txn.quotes";

    @Autowired
    KafkaTemplate<String, String> kafkaTemplateTransactional;

    @Transactional
    @KafkaListener(id = "txn-listener-1", topics = TOPIC)
    public void txnSend(String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        if (!kafkaTemplateTransactional.isTransactional()) {
            log.error("KafkaTemplate is not transactional");
            return;
        }

        ProducerRecord<String, String> r = new ProducerRecord<>(TRANSACTIONAL_TOPIC, key, message);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplateTransactional.send(r);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send message", ex);
            } else {
                log.info("Sent message: {}", result);
            }
        });
        dbUpdate();
    }

    @SneakyThrows
    public void dbUpdate() {
        Thread.sleep(5000);
    }
}
