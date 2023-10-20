package com.github.prametta.springkafkaclient;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;

@SpringBootTest
@Log4j2
class SpringKafkaClientApplicationTests {

    @BeforeAll
    static void setup() {
        // https://java.testcontainers.org/modules/kafka/
        KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"))
                .withKraft()
                .withListener(() -> "localhost:19092");
        kafka.setPortBindings(Collections.singletonList("19092:19092"));
        kafka.start();
        log.info("Kafka started");
    }

    @Test
    void contextLoads() {
    }

}
