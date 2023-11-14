package com.bhatman.poc.astra.flight;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface AirportRepo extends Repository<Airport, String> {

	<S extends Airport> S save(S entity);
	
	Optional<Airport> findById(String airport);

	Iterable<Airport> findAll();

	long count();

	void delete(Airport entity);

	void deleteById(String airport);

	boolean existsById(String airport);

}
