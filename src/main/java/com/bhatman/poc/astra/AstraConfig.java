package com.bhatman.poc.astra;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

import lombok.Data;

@Data
@Profile("!local")
@ConfigurationProperties(prefix = "datastax.astra")
public class AstraConfig {
	private File secureConnectBundle;
}
