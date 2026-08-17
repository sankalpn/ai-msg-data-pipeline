package com.acuvity.pipeline.serviceconfig;

import com.acuvity.pipeline.service.log.LogServiceApi;
import com.acuvity.pipeline.service.sync.PipelineSyncApi;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class LogServiceConfiguration {

    @Bean
    HttpClient logServiceHttpClient(PipelineProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(properties.logService().connectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @Bean
    WebClient logServiceWebClient(HttpClient logServiceHttpClient, PipelineProperties properties) {
        var connector = new JdkClientHttpConnector(logServiceHttpClient);
        connector.setReadTimeout(properties.logService().requestTimeout());
        return WebClient.builder()
                .baseUrl(properties.logService().baseUrl().toString())
                .clientConnector(connector)
                .build();
    }

    @Bean
    LogServiceApi logServiceApi(WebClient logServiceWebClient) {
        var adapter = WebClientAdapter.create(logServiceWebClient);
        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(LogServiceApi.class);
    }

    @Bean
    PipelineSyncApi pipelineSyncApi(HttpClient logServiceHttpClient, PipelineProperties properties) {
        var connector = new JdkClientHttpConnector(logServiceHttpClient);
        connector.setReadTimeout(properties.logService().requestTimeout());
        var webClient = WebClient.builder()
                .baseUrl(properties.serviceBaseUrl().toString())
                .clientConnector(connector)
                .build();
        var adapter = WebClientAdapter.create(webClient);
        return HttpServiceProxyFactory.builderFor(adapter).build().createClient(PipelineSyncApi.class);
    }
}
