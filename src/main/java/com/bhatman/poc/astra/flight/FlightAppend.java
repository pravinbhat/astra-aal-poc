package com.bhatman.poc.astra.flight;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.update;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.update.UpdateStart;

@Service
public class FlightAppend {

	List<SimpleStatement> updateWithAppendEntry(Flight flight) {
		UpdateStart update = update("flight");
		return flight.getActualEvent().entrySet().stream().map(entry -> {
			return update.appendMapEntry("actual_event", literal(entry.getKey()), literal(entry.getValue()))
					.setColumn("flight_name", literal(flight.getFlightName()))
					.setColumn("flight_details", literal(flight.getFlightDetails())).whereColumn("airport_id")
					.isEqualTo(literal(flight.getFlightPk().getAirportId())).whereColumn("flight_id")
					.isEqualTo(literal(flight.getFlightPk().getFlightId())).build();
		}).collect(Collectors.toList());
	}

}
