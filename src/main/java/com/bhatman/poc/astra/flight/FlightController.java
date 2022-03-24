package com.bhatman.poc.astra.flight;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FlightController {

    Map<Long, Flight> flights = new HashMap<>();

    @GetMapping("/flights")
    List<Flight> all() {
        return new ArrayList<>(flights.values());
    }

    @PostMapping("/flights")
    Flight newFlight(@RequestBody Flight newFlight) {
        flights.put(newFlight.getFlightId(), newFlight);

        return newFlight;
    }

    @GetMapping("/flights/{id}")
    Flight one(@PathVariable Long id) {
        return flights.get(id);
    }

    @PutMapping("/flights/{id}")
    Flight replaceFlight(@RequestBody Flight newFlight, @PathVariable Long id) {
        flights.put(id, newFlight);

        return newFlight;
    }

    @DeleteMapping("/flights/{id}")
    void deleteFlight(@PathVariable Long id) {
        flights.remove(id);
    }

}
