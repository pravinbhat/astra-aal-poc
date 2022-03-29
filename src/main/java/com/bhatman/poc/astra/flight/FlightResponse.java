package com.bhatman.poc.astra.flight;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlightResponse {
	private Flight flight;
	private String message;
}
