package com.bhatman.poc.astra;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "datastax.astra")
public class AstraConfig {
	private File secureConnectBundle;
}
