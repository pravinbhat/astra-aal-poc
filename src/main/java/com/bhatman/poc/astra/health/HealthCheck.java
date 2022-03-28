package com.bhatman.poc.astra.health;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HealthCheck {
	@Autowired
	private RestTemplate restTemplate;

	@Value("${astra.dbid}")
	String astraDbId;

	@Value("${astra.token}")
	String astraToken;

	private final static String ASTRA_HEALTH_URL = "https://api.astra.datastax.com/v2/databases/";
	private final static String STATUS_ONLINE = "ONLINE";
	private HttpEntity<String> entity = null;

	private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

	public boolean isHealthly(Exception e) {
		logger.warn("Received exception " + e.getMessage());
		if (null == entity) {
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.setBearerAuth(astraToken);
			entity = new HttpEntity<String>(headers);
		}

		ResponseEntity<Health[]> healthStatus = restTemplate.exchange(ASTRA_HEALTH_URL + astraDbId + "/datacenters",
				HttpMethod.GET, entity, Health[].class);
		logger.warn("Astra health status " + healthStatus.getBody()[0]);
		if (STATUS_ONLINE.equalsIgnoreCase(healthStatus.getBody()[0].getStatus())) {
			return true;
		}

		return false;
	}
}
