package com.bhatman.poc.astra.health;

import java.util.Arrays;

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

	String health_url = "https://api.astra.datastax.com/v2/databases/";

	public boolean isHealthly() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setBearerAuth(astraToken);
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		ResponseEntity<Health[]> healthStatus = restTemplate.exchange(health_url + astraDbId + "/datacenters",
				HttpMethod.GET, entity, Health[].class);
		if ("ONLINE".equalsIgnoreCase(healthStatus.getBody()[0].getStatus())) {
			System.out.println("Astra status is " + healthStatus.getBody()[0]);
			return true;
		}

		return false;
	}
}
