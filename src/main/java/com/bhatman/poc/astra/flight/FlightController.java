package com.bhatman.poc.astra.flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhatman.poc.astra.health.HealthCheck;
import com.datastax.oss.driver.api.core.uuid.Uuids;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/flights")
public class FlightController {

	private static final String Flight_CircuitBreaker = "FlightController";
	Map<Long, Flight> flights = new HashMap<>();

	@Autowired
	HealthCheck hc;

	@Autowired
	FlightRepo flightRepo;

	@GetMapping
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorAllFlights")
	public ResponseEntity<List<Flight>> all() throws Exception {
		List<Flight> flights = new ArrayList<Flight>();
		flightRepo.findAll().forEach(flights::add);

		if (flights.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(flights, HttpStatus.OK);
	}

	public ResponseEntity<List<Flight>> healthErrorAllFlights(Exception e) {
		if (!hc.isHealthly()) {
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@PostMapping
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<Flight> add(@RequestBody Flight newFlight) {
		Flight flight = flightRepo.save(new Flight(Uuids.timeBased(), newFlight.getFlightName()));
		return new ResponseEntity<>(flight, HttpStatus.CREATED);
	}

	@GetMapping("/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<Flight> get(@PathVariable UUID flightId) {
		Optional<Flight> flight = flightRepo.findById(flightId);

		if (flight.isPresent()) {
			return new ResponseEntity<>(flight.get(), HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PutMapping("/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<Flight> update(@RequestBody Flight updateFlight, @PathVariable UUID flightId) {
		Assert.isTrue(flightId.equals(updateFlight.getFlightId()),
				"Flight Id provided does not match the value in path");
		Objects.requireNonNull(updateFlight);

		return new ResponseEntity<>(flightRepo.save(updateFlight), HttpStatus.OK);

	}

	public ResponseEntity<List<Flight>> healthErrorOneFlight(Exception e) {
		if (!hc.isHealthly()) {
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@DeleteMapping("/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorHttpStatus")
	public ResponseEntity<HttpStatus> delete(@PathVariable UUID flightId) {
		flightRepo.deleteById(flightId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<HttpStatus> healthErrorHttpStatus(Exception e) {
		if (!hc.isHealthly()) {
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
