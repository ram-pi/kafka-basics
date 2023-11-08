package com.github.prametta.springkafkaclient;

import com.github.javafaker.Faker;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
public class KafkaProducer {
    final static String TOPIC = "quotes";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 1000)
    public void send() {
        Faker faker = new Faker();
        var v = faker.gameOfThrones().quote();
        var k = faker.gameOfThrones().character();
        ProducerRecord<String, String> r = new ProducerRecord<>(TOPIC, k, v);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(r);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send message", ex);
            } else {
                log.info("Sent message: {}", result);
            }
        });
    }
}
