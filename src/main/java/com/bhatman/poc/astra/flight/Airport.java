package com.bhatman.poc.astra.flight;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Table(value = "airport")
public class Airport {
	@PrimaryKey
	private String airport;

	@Column(value = "airport_id")
	private String airportId;

}
