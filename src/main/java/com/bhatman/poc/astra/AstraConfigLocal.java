package com.bhatman.poc.astra;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

import lombok.Data;

@Data
@Profile("local")
@ConfigurationProperties(prefix = "cassandra.metrics")
public class AstraConfigLocal {
	private List<String> sessionMetrics = new ArrayList<>();

	private List<String> nodeMetrics = new ArrayList<>();
}
