package com.acuvity.pipeline;

import com.acuvity.pipeline.serviceconfig.PipelineProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PipelineProperties.class)
public class DataPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataPipelineApplication.class, args);
    }
}
