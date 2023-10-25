package com.bhatman.poc.astra;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

import lombok.Data;

@Data
@Profile("!local")
@ConfigurationProperties(prefix = "datastax.astra")
public class AstraConfig {
	private File secureConnectBundle;
	private List<String> sessionMetrics = new ArrayList<>();
	private List<String> nodeMetrics = new ArrayList<>();	
}
