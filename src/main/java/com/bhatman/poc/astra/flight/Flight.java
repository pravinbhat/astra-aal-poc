package com.bhatman.poc.astra.flight;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Table

public class Flight {
	@PrimaryKey(value = "flight_id")
	private UUID flightId;
	
	@Column(value = "flight_name")
	private String flightName;
}
