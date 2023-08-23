package com.bhatman.poc.astra.flight;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.update;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.querybuilder.update.UpdateStart;

@Service
public class FlightAppend {

	List<Statement> updateWithAppendEntry(Flight flight) {
		UpdateStart update = update("flight");
		return flight.getActualEvent().entrySet().stream().map(entry -> {
			return update.appendMapEntry("actual_event", literal(entry.getKey()), literal(entry.getValue()))
					.setColumn("flight_name", literal(flight.getFlightName()))
					.setColumn("flight_details", literal(flight.getFlightDetails()))
					.whereColumn("flight_id")
					.isEqualTo(literal(flight.getFlightId())).build();
		}).collect(Collectors.toList());
	}

}
