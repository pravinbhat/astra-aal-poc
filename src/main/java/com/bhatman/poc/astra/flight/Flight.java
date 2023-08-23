package com.bhatman.poc.astra.flight;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Table(value = "flight")
public class Flight {
	@PrimaryKey
	private FlightPk flightPk;

	@Column(value = "flight_name")
	private String flightName;

	@Column(value = "flight_details")
	private String flightDetails;

	@Column(value = "actual_event")
	private Map<String, Instant> actualEvent;
}
