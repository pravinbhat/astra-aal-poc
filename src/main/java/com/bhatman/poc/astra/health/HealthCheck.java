package com.bhatman.poc.astra.health;

import java.util.Arrays;
import java.util.Optional;

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
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;

@Service
public class HealthCheck {
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	CqlSession cqlSession;

	@Value("${astra.dbid}")
	String astraDbId;

	@Value("${astra.token}")
	String astraToken;
	
	@Value("${spring.data.cassandra.keyspace-name}")
	String keyspaceName;

	private final static String ASTRA_HEALTH_URL = "https://api.astra.datastax.com/v2/databases/";
	private final static String STATUS_ONLINE = "ONLINE";
	private final static String VALID_KEYSPACE = "replication";
	
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
		logger.info("Your Astra region health status is " + healthStatus.getBody()[0]);
		if (STATUS_ONLINE.equalsIgnoreCase(healthStatus.getBody()[0].getStatus())) {
			logger.info("Checking keyspace is accessible within Astra region");
			KeyspaceMetadata ksMeta =  cqlSession.getMetadata().getKeyspace(keyspaceName).get();
			if (ksMeta.describe(true).contains(VALID_KEYSPACE)) {
				logger.info("Keyspace " + keyspaceName + " is accessible with Metadata as " + ksMeta.describe(true));
				return true;
			}
			logger.error("Keyspace " + keyspaceName + " is not accessible");
		}

		return false;
	}
}
