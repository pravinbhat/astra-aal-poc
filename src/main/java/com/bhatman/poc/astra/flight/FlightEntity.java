package com.bhatman.poc.astra.flight;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(defaultKeyspace = "test_ks")
@CqlName("flight")
public class FlightEntity {
	@PartitionKey
	@CqlName("flight_id")
	private UUID flightId;

	@CqlName("flight_name")
	private String flightName;

	@CqlName("flight_details")
	private String flightDetails;

	@CqlName("actual_event")
	private Map<String, Instant> actualEvent;
}
