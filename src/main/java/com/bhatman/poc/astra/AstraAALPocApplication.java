package com.bhatman.poc.astra;

import java.nio.file.Path;
import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties(AstraConfig.class)
public class AstraAALPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(AstraAALPocApplication.class, args);
	}

	/**
	 * Used to connect to Astra via secure-connect-bundle
	 */
	@Bean
	@Profile("!local")
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(AstraConfig astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.setConnectTimeout(Duration.ofMillis(3000)).setReadTimeout(Duration.ofMillis(3000)).build();
	}
}
