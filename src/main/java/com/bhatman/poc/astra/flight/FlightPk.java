package com.bhatman.poc.astra.flight;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@PrimaryKeyClass
public class FlightPk {
	@PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "airport_id")
	private String airportId;

	@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "flight_id")
	private UUID flightId;
}
