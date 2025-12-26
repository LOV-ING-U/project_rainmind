package com.project.rainmind.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class KmaExternalFetchClientConfig {
    // Bean 으로 만들어 주입
    @Bean
    fun weatherNowFetchClient(): RestClient =
        RestClient
            .builder()
            .baseUrl("https://apis.data.go.kr")
            .defaultHeader("Accept", "application/json")
            .build()
}