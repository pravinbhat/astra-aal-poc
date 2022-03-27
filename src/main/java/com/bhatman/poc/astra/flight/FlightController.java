package com.bhatman.poc.astra.flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/flights")
public class FlightController {

	private static final String Flight_CircuitBreaker = "FlightController";
	Map<Long, Flight> flights = new HashMap<>();

	@GetMapping
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "sendAstraRegionErrorMsg")
	public List<Flight> all() throws Exception {
		if (Math.random() < 0.6) {
			throw new Exception("Unable to get Flight data!");
		}
		return new ArrayList<>(flights.values());
	}

	public List<Flight> sendAstraRegionErrorMsg(Exception e) {
		List<Flight> ret = new ArrayList<>();
		ret.add(new Flight(0l, "Fallback: " + e.getMessage()));

		return ret;
	}

	@PostMapping
	public Flight newFlight(@RequestBody Flight newFlight) {
		flights.put(newFlight.getFlightId(), newFlight);

		return newFlight;
	}

	@GetMapping("/{id}")
	public Flight one(@PathVariable Long id) {
		return flights.get(id);
	}

	@PutMapping("/{id}")
	public Flight replaceFlight(@RequestBody Flight newFlight, @PathVariable Long id) {
		flights.put(id, newFlight);

		return newFlight;
	}

	@DeleteMapping("/{id}")
	public void deleteFlight(@PathVariable Long id) {
		flights.remove(id);
	}

}
