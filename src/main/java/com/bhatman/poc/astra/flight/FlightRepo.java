package com.bhatman.poc.astra.flight;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.Repository;

public interface FlightRepo extends Repository<Flight, FlightPk> {

	<S extends Flight> S save(S entity);

	@Query("UPDATE flight SET flight_name = ?2, flight_details = ?3, actual_event = actual_event + ?4 "
			+ "WHERE airport_id = ?0 and flight_id = ?1")
	void appendToActualEvent(String airportId, UUID flightId, String flightName, String flightDetails,
			Map<String, Instant> eventMap);

	Optional<Flight> findById(FlightPk flightPk);

	Iterable<Flight> findAll();

	long count();

	void delete(Flight entity);

	void deleteById(FlightPk flightPk);

	boolean existsById(FlightPk flightPk);

	@Query(value = "TRUNCATE flight")
	void deleteAll();
}
