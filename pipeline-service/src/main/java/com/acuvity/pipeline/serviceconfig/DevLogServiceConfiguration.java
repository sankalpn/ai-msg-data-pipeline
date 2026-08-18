package com.acuvity.pipeline.serviceconfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Configuration(proxyBeanMethods = false)
@Profile("dev")
public class DevLogServiceConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    WireMockServer logServiceWireMock(
            @Value("${pipeline.dev.log-service.port:8081}") int port) {
        return new WireMockServer(options()
                .port(port)
                .usingFilesUnderClasspath("wiremock"));
    }
}
