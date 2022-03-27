package com.bhatman.poc.astra.flight;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface FlightRepo extends Repository<Flight, Long> {
	<S extends Flight> S save(S entity);

	Optional<Flight> findById(Long primaryKey);

	Iterable<Flight> findAll();

	long count();

	void delete(Flight entity);

	boolean existsById(Long primaryKey);
}
