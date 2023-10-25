package com.bhatman.poc.astra;

import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.METRICS_NODE_ENABLED;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.METRICS_SESSION_ENABLED;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.SESSION_NAME;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.REQUEST_CONSISTENCY;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.REQUEST_DEFAULT_IDEMPOTENCE;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.SPECULATIVE_EXECUTION_DELAY;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.SPECULATIVE_EXECUTION_MAX;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.SPECULATIVE_EXECUTION_POLICY_CLASS;

import java.nio.file.Path;
import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.cassandra.DriverConfigLoaderBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.datastax.oss.driver.api.core.CqlSession;

@SpringBootApplication
@EnableConfigurationProperties({ AstraConfig.class, AstraConfigLocal.class })
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
		return builder -> {
			builder.withCloudSecureConnectBundle(bundle);
		};
	}

	@Bean
	@Profile("!local")
	DriverConfigLoaderBuilderCustomizer configLoaderBuilderCustomizer(AstraConfig astraProperties) {
		return builder -> {
			builder.withStringList(METRICS_SESSION_ENABLED, astraProperties.getSessionMetrics());
			builder.withStringList(METRICS_NODE_ENABLED, astraProperties.getNodeMetrics());
			builder.withString(SESSION_NAME, "bhatman");
		};
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.setConnectTimeout(Duration.ofMillis(3000)).setReadTimeout(Duration.ofMillis(3000)).build();
	}

	@Bean
	@Profile("local")
	DriverConfigLoaderBuilderCustomizer configLoaderBuilderCustomizer(AstraConfigLocal cassandraProperties) {
		return builder -> {
//          builder.withBoolean(REQUEST_DEFAULT_IDEMPOTENCE, true);
//        	builder.withClass(SPECULATIVE_EXECUTION_POLICY_CLASS, ConstantSpeculativeExecutionPolicy.class);
//        	builder.withInt(SPECULATIVE_EXECUTION_MAX, 3);
//        	builder.withDuration(SPECULATIVE_EXECUTION_DELAY, Duration.ofMillis(1));
			builder.withString(REQUEST_CONSISTENCY, "LOCAL_QUORUM");

            builder.withString(REQUEST_DEFAULT_IDEMPOTENCE, "true");
			builder.withString(SPECULATIVE_EXECUTION_POLICY_CLASS, "ConstantSpeculativeExecutionPolicy");
			builder.withString(SPECULATIVE_EXECUTION_MAX, "3");
			builder.withString(SPECULATIVE_EXECUTION_DELAY, "2 milliseconds");

			builder.withStringList(METRICS_SESSION_ENABLED, cassandraProperties.getSessionMetrics());
			builder.withStringList(METRICS_NODE_ENABLED, cassandraProperties.getNodeMetrics());
		};
	}

	@Bean
	public MetricRegistry getMetricsbean(CqlSession cqlSession) {
		return cqlSession.getMetrics().orElseThrow(() -> new IllegalStateException("Metrics are disabled"))
				.getRegistry();
	}

	@Bean
	public JmxReporter getJmxReporter(MetricRegistry registry) {
		JmxReporter reporter = JmxReporter.forRegistry(registry).inDomain("bhatman.driver.metrics").build();
		reporter.start();
		return reporter;
	}
}
