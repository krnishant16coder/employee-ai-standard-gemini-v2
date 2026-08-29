package com.example.employeeai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeminiConfig.class)
public class AppConfig {

 @Bean
 public RestClient geminiRestClient() {
  return RestClient.builder()
          .baseUrl(
                  "https://generativelanguage.googleapis.com"
          )
          .build();
 }
}