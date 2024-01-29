package com.bhatman.poc.astra.flight;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.update;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import com.datastax.oss.driver.api.querybuilder.update.UpdateStart;

@Service
public class FlightAppend {
	@Autowired
	CodecRegistry cr;

	List<SimpleStatement> updateWithAppendEntry(Flight flight) {
		UpdateStart update = update("flight");
		List<SimpleStatement> ssList = new ArrayList<>();
		ssList.add(update.setColumn("flight_name", literal(flight.getFlightName()))
		.setColumn("flight_details", literal(flight.getFlightDetails())).whereColumn("airport_id")
		.isEqualTo(literal(flight.getFlightPk().getAirportId())).whereColumn("flight_id")
		.isEqualTo(literal(flight.getFlightPk().getFlightId())).build());

		if (!CollectionUtils.isEmpty(flight.getActualEvent())) {
			ssList.addAll(flight.getActualEvent().entrySet().stream().map(entry -> {
				return update.appendMapEntry("actual_event", literal(entry.getKey()), literal(entry.getValue()))
						.whereColumn("airport_id")
						.isEqualTo(literal(flight.getFlightPk().getAirportId())).whereColumn("flight_id")
						.isEqualTo(literal(flight.getFlightPk().getFlightId())).build();
			}).collect(Collectors.toList()));
		}

		if (!CollectionUtils.isEmpty(flight.getSysLocalHist())) {
			ssList.addAll(flight.getSysLocalHist().stream().map(ele -> {
				return update.appendListElement("sys_local_hist", literal(ele, cr))
						.whereColumn("airport_id")
						.isEqualTo(literal(flight.getFlightPk().getAirportId())).whereColumn("flight_id")
						.isEqualTo(literal(flight.getFlightPk().getFlightId())).build();
			}).collect(Collectors.toList()));
		}
		
		return ssList;
	}

}
