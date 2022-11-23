package com.bhatman.poc.astra.flight;

import java.util.ArrayList;
import java.util.List;
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
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/flights")
public class FlightController {

	private static final String Flight_CircuitBreaker = "FlightController";

	@Autowired
	HealthCheck hc;

	@Autowired
	FlightRepo flightRepo;

	@Autowired
	CqlSession cqlSession;

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
		if (!hc.isHealthly(e)) {
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@PostMapping
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> add(@RequestBody Flight newFlight) {
		Flight flight = flightRepo.save(new Flight(Uuids.timeBased(), newFlight.getFlightName()));
		return new ResponseEntity<>(new FlightResponse(flight, "Flight created!"), HttpStatus.CREATED);
	}

	@GetMapping("/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> get(@PathVariable UUID flightId) {
		Optional<Flight> flight = flightRepo.findById(flightId);

		if (flight.isPresent()) {
			return new ResponseEntity<>(new FlightResponse(flight.get(), "Flight found!"), HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PutMapping("/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> update(@RequestBody Flight updateFlight, @PathVariable UUID flightId) {
		Assert.isTrue(flightId.equals(updateFlight.getFlightId()),
				"Flight Id provided does not match the value in path");
		Objects.requireNonNull(updateFlight);
		ResponseEntity re = get(flightId);
		Assert.isTrue(re.getStatusCode().equals(HttpStatus.OK), "No such Flight exists for Id " + flightId);

		return new ResponseEntity<>(new FlightResponse(flightRepo.save(updateFlight), "Flight updated!"),
				HttpStatus.OK);

	}

	public ResponseEntity<FlightResponse> healthErrorOneFlight(Exception e) {
		if (!hc.isHealthly(e)) {
			return new ResponseEntity<>(new FlightResponse(null, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
		}

		return new ResponseEntity<>(new FlightResponse(null, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@DeleteMapping("/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorHttpStatus")
	public ResponseEntity<HttpStatus> delete(@PathVariable UUID flightId) {
		flightRepo.deleteById(flightId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// This (spring-data based truncate) works with C* & DSE but not Astra
	@DeleteMapping("/all")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorHttpStatus")
	public ResponseEntity<HttpStatus> deleteAll() {
		flightRepo.deleteAll();
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// This works with everything including Astra
	@DeleteMapping("/truncate")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorHttpStatus")
	public ResponseEntity<HttpStatus> deleteAllNative() {
		cqlSession.execute(QueryBuilder.truncate(CqlIdentifier.fromCql("flight")).build());

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<HttpStatus> healthErrorHttpStatus(Exception e) {
		if (!hc.isHealthly(e)) {
			return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
