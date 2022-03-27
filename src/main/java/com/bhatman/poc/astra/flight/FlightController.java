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

import com.datastax.oss.driver.api.core.uuid.Uuids;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/flights")
public class FlightController {

	private static final String Flight_CircuitBreaker = "FlightController";
	Map<Long, Flight> flights = new HashMap<>();

	@Autowired
	FlightRepo flightRepo;

	@GetMapping
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "sendAstraRegionErrorMsg")
	public ResponseEntity<List<Flight>> all() throws Exception {
		if (Math.random() < 0.4) {
			throw new Exception("Unable to get Flight data!");
		}
		try {
			List<Flight> flights = new ArrayList<Flight>();
			flightRepo.findAll().forEach(flights::add);

			if (flights.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(flights, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<List<Flight>> sendAstraRegionErrorMsg(Exception e) {
		List<Flight> ret = new ArrayList<>();
		ret.add(new Flight(UUID.randomUUID(), "Fallback: " + e.getMessage()));

		return new ResponseEntity<>(ret, HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<Flight> newFlight(@RequestBody Flight newFlight) {
		try {
			Flight _flight = flightRepo.save(new Flight(Uuids.timeBased(), newFlight.getFlightName()));
			return new ResponseEntity<>(_flight, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<Flight> one(@PathVariable UUID id) {
		Optional<Flight> flight = flightRepo.findById(id);

		if (flight.isPresent()) {
			return new ResponseEntity<>(flight.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<Flight> replaceFlight(@RequestBody Flight newFlight, @PathVariable UUID id) {
		Assert.isTrue(id.equals(newFlight.getFlightId()), "Flight Id provided does not match the value in path");
		Objects.requireNonNull(newFlight);

		return new ResponseEntity<>(flightRepo.save(newFlight), HttpStatus.OK);

	}

	@DeleteMapping("/{id}")
	public ResponseEntity<HttpStatus> deleteFlight(@PathVariable UUID id) {
		try {
			flightRepo.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
