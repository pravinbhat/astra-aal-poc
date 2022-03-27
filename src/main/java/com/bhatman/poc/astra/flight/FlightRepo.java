package com.bhatman.poc.astra.flight;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

public interface FlightRepo extends Repository<Flight, UUID> {
	<S extends Flight> S save(S entity);

	Optional<Flight> findById(UUID primaryKey);

	Iterable<Flight> findAll();

	long count();

	void delete(Flight entity);
	void deleteById(UUID flightId);

	boolean existsById(UUID primaryKey);
}
