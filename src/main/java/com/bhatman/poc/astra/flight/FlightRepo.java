package com.bhatman.poc.astra.flight;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.Repository;

public interface FlightRepo extends Repository<Flight, UUID> {

	<S extends Flight> S save(S entity);

	@Query("UPDATE flight SET flight_name = ?1, flight_details = ?2, actual_event = actual_event + ?3 WHERE flight_id = ?0")
	void appendToActualEvent(UUID flightId, String flightName, String flightDetails, Map<String, Instant> eventMap);

	Optional<Flight> findById(UUID primaryKey);

	Iterable<Flight> findAll();

	long count();

	void delete(Flight entity);

	void deleteById(UUID flightId);

	boolean existsById(UUID primaryKey);

	@Query(value = "TRUNCATE flight")
	void deleteAll();
}
