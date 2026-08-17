package com.acuvity.pipeline.serviceconfig;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(properties = "spring.main.web-application-type=none")
class KafkaAutoConfigurationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void createsKafkaTemplate() {
        assertThat(kafkaTemplate).isNotNull();
    }
}
