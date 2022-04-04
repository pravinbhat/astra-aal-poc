package com.bhatman.poc.astra.health;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.Metadata;

@Service
public class HealthCheck {
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	CqlSession cqlSession;

	@Value("${astra.db.id}")
	String astraDbId;

	@Value("${astra.db.application.token}")
	String astraToken;

	@Value("${astra.db.timeout}")
	Long astraTimeout;

	@Value("${astra.db.keyspace}")
	String keyspaceName;

	private final static String ASTRA_HEALTH_URL = "https://api.astra.datastax.com/v2/databases/";
	private final static String STATUS_ONLINE = "ONLINE";

	private HttpEntity<String> entity = null;

	private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

	public boolean isHealthly(Exception e) {
		CompletableFuture<Metadata> results = checkHealthly(e);
		if (null == results) {
			return false;
		}
		try {
			Metadata ksm = results.get(astraTimeout, TimeUnit.SECONDS);
			logger.info(
					"Keyspace is accessible within Astra region " + ksm.getKeyspace(keyspaceName).get().describe(true));
		} catch (Exception e1) {
			logger.error("Keyspace " + keyspaceName + " is not accessible within Astra region!!");
			e1.printStackTrace();
			return false;
		}

		return true;
	}

	public CompletableFuture<Metadata> checkHealthly(Exception e) {
		logger.warn("Received exception " + e.getMessage());
		ResponseEntity<Health[]> healthStatus = restTemplate.exchange(ASTRA_HEALTH_URL + astraDbId + "/datacenters",
				HttpMethod.GET, getHttpEntity(), Health[].class);
		logger.info("Your Astra region health status is " + healthStatus.getBody()[0]);
		if (STATUS_ONLINE.equalsIgnoreCase(healthStatus.getBody()[0].getStatus())) {
			logger.info("Checking keyspace is accessible within Astra region");
			return CompletableFuture.supplyAsync(() -> cqlSession.refreshSchema());
		}

		return null;
	}

	private HttpEntity<String> getHttpEntity() {
		if (null == entity) {
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.setBearerAuth(astraToken);
			entity = new HttpEntity<String>(headers);
		}

		return entity;
	}

}
