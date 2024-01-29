package com.bhatman.poc.astra.flight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.metadata.TokenMap;
import com.datastax.oss.driver.api.core.metadata.token.TokenRange;
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
	AirportRepo airportRepo;

	@Autowired
	FlightAppend flightAppend;

	@Autowired
	CqlSession cqlSession;

	@GetMapping
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorAllFlights")
	public ResponseEntity<List<Flight>> all() throws Exception {
		List<Flight> flights = new ArrayList<>();
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
		newFlight.getFlightPk().setFlightId(Uuids.timeBased());
		Flight flight = flightRepo.save(newFlight);
		return new ResponseEntity<>(new FlightResponse(flight, "Flight created!"), HttpStatus.CREATED);
	}

	@GetMapping("/{airportId}/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> get(@PathVariable String airportId, @PathVariable UUID flightId) {
		FlightPk fPk = new FlightPk(airportId, flightId);
		Optional<Flight> flight = flightRepo.findById(fPk);

		if (flight.isPresent()) {
			return new ResponseEntity<>(new FlightResponse(flight.get(), "Flight found!"), HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/xref/{airport}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<Airport> getAirportXref(@PathVariable String airport) {
		Optional<Airport> airportObj = airportRepo.findById(airport);

		if (airportObj.isPresent()) {
			return new ResponseEntity<>(airportObj.get(), HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/{airportId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<List<Flight>> get(@PathVariable String airportId) {
		List<Flight> flights = new ArrayList<>();
		flightRepo.findByAirportId(airportId).forEach(flights::add);

		if (flights.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(flights, HttpStatus.OK);
	}
	
	@PostMapping("/{airportId}/{flightCount}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> addFlights(@PathVariable String airportId, @PathVariable int flightCount) {
		return addFlights(airportId, flightCount, 1);
	}

	@PostMapping("/{airportId}/{flightCount}/{startIndex}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> addFlights(@PathVariable String airportId, @PathVariable int flightCount, @PathVariable int startIndex) {
		List<Flight> flights = new ArrayList<>();
		for (int i = startIndex; i < startIndex + flightCount; i++) {
			Flight newFlight = new Flight(new FlightPk(airportId, null), 
					airportId + "-flight-" + i, "This is flight number " + i + " from airport. Hope to see you soon!" + airportId, null, null);
			newFlight.getFlightPk().setFlightId(Uuids.timeBased());
			flights.add(newFlight);
		}
		flightRepo.saveAll(flights);

		return new ResponseEntity<>(new FlightResponse(null, flightCount + " flights created!"), HttpStatus.CREATED);
	}
	
	@PostMapping("/airports")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<List<Flight>> flightXRef(@RequestBody String airports) {
		List<Flight> allFlights = new ArrayList<>();
		List<String> airportList = Arrays.asList(airports.split(","));
		airportList.parallelStream().forEach(a -> {
			Optional<Airport> airportObj = airportRepo.findById(a);
			List<Flight> flights = get(airportObj.get().getAirportId()).getBody();
			allFlights.addAll(flights);
		});

		if (allFlights.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(allFlights, HttpStatus.OK);
	}
	
	@PutMapping("/{airportId}/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> update(@RequestBody Flight updateFlight, @PathVariable String airportId,
			@PathVariable UUID flightId) {
		Assert.isTrue(flightId.equals(updateFlight.getFlightPk().getFlightId()),
				"Flight Id provided does not match the value in path");

		Metadata metadata = cqlSession.getMetadata();

		Optional<TokenMap> optionalTokenMap = metadata.getTokenMap();

		if (optionalTokenMap.isPresent()) {
			TokenMap tokenMap = optionalTokenMap.get();
			for (Node node : metadata.getNodes().values()) {
				Set<TokenRange> ranges = tokenMap.getTokenRanges(node);
				System.out.println("Node: " + node.getEndPoint() + " has token ranges: " + ranges);
			}
		} else {
			System.out.println("Token map is not available.");
		}

		Objects.requireNonNull(updateFlight);
		ResponseEntity<FlightResponse> re = get(airportId, flightId);
		Assert.isTrue(re.getStatusCode().equals(HttpStatus.OK), "No such Flight exists for Id " + flightId);

		return new ResponseEntity<>(new FlightResponse(flightRepo.save(updateFlight), "Flight updated!"),
				HttpStatus.OK);

	}

	@PutMapping("/{airportId}/{flightId}/dao")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> updateDao(@RequestBody Flight updateFlight, @PathVariable String airportId,
			@PathVariable UUID flightId) {
		Assert.isTrue(flightId.equals(updateFlight.getFlightPk().getFlightId()),
				"Flight Id provided does not match the value in path");

		Objects.requireNonNull(updateFlight);
		ResponseEntity<FlightResponse> re = get(airportId, flightId);
		Assert.isTrue(re.getStatusCode().equals(HttpStatus.OK), "No such Flight exists for Id " + flightId);
//		FlightMapper mapper = new FlightMapperBuilder(cqlSession).build();
//		FlightDAO flightDAO = mapper.flightDao();
//		flightDAO.update(new Flight(updateFlight.getFlightId(), updateFlight.getFlightName(),
//				updateFlight.getActualEvent()));

		BatchStatementBuilder batchBldr = BatchStatement.builder(BatchType.UNLOGGED);
		flightAppend.updateWithAppendEntry(updateFlight).forEach(batchBldr::addStatement);
		cqlSession.execute(batchBldr.build());

		FlightPk fPk = new FlightPk(airportId, flightId);

		return new ResponseEntity<>(new FlightResponse(flightRepo.findById(fPk).get(), "Flight updated via DAO!"),
				HttpStatus.OK);

	}

	@PutMapping("/bulk-put")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<HttpStatus> updateBulk(@RequestBody List<Flight> updateFlights) {
		Objects.requireNonNull(updateFlights);
		updateFlights.stream().forEach(f -> {
			ResponseEntity<FlightResponse> re = get(f.getFlightPk().getAirportId(), f.getFlightPk().getFlightId());
			Assert.isTrue(re.getStatusCode().equals(HttpStatus.OK),
					"No such Flight exists for Id " + f.getFlightPk().getFlightId());
			flightAppend.updateWithAppendEntry(f).forEach(cqlSession::execute);
		});

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PutMapping("/batch-put")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<HttpStatus> updateBatch(@RequestBody List<Flight> updateFlights) {
		Objects.requireNonNull(updateFlights);
		BatchStatementBuilder batchBldr = BatchStatement.builder(BatchType.LOGGED);
		updateFlights.forEach(f -> {
			ResponseEntity<FlightResponse> re = get(f.getFlightPk().getAirportId(), f.getFlightPk().getFlightId());
			Assert.isTrue(re.getStatusCode().equals(HttpStatus.OK),
					"No such Flight exists for Id " + f.getFlightPk().getFlightId());
			flightAppend.updateWithAppendEntry(f).forEach(batchBldr::addStatement);
		});

		cqlSession.execute(batchBldr.build());

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PutMapping("/unlogged-batch-put")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<HttpStatus> updateUnloggedBatch(@RequestBody List<Flight> updateFlights) {
		Objects.requireNonNull(updateFlights);
		BatchStatementBuilder batchBldr = BatchStatement.builder(BatchType.UNLOGGED);
		updateFlights.forEach(f -> {
			ResponseEntity<FlightResponse> re = get(f.getFlightPk().getAirportId(), f.getFlightPk().getFlightId());
			Assert.isTrue(re.getStatusCode().equals(HttpStatus.OK),
					"No such Flight exists for Id " + f.getFlightPk().getFlightId());
			flightAppend.updateWithAppendEntry(f).forEach(batchBldr::addStatement);
		});

		cqlSession.execute(batchBldr.build());

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PutMapping("/{airportId}/{flightId}/event")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorOneFlight")
	public ResponseEntity<FlightResponse> updateMap(@RequestBody Flight updateFlight, @PathVariable String airportId,
			@PathVariable UUID flightId) {
		Assert.isTrue(flightId.equals(updateFlight.getFlightPk().getFlightId()),
				"Flight Id provided does not match the value in path");

		Metadata metadata = cqlSession.getMetadata();

		Optional<TokenMap> optionalTokenMap = metadata.getTokenMap();

		if (optionalTokenMap.isPresent()) {
			TokenMap tokenMap = optionalTokenMap.get();
			for (Node node : metadata.getNodes().values()) {
				Set<TokenRange> ranges = tokenMap.getTokenRanges(node);
				System.out.println("Node: " + node.getEndPoint() + " has token ranges: " + ranges);
			}
		} else {
			System.out.println("Token map is not available.");
		}

		Objects.requireNonNull(updateFlight);
		ResponseEntity<FlightResponse> re = get(airportId, flightId);
		Assert.isTrue(re.getStatusCode().equals(HttpStatus.OK), "No such Flight exists for Id " + flightId);
		FlightPk fPk = new FlightPk(airportId, flightId);
		flightRepo.appendToActualEvent(fPk.getAirportId(), fPk.getFlightId(), updateFlight.getFlightName(),
				updateFlight.getFlightDetails(), updateFlight.getActualEvent());

		return new ResponseEntity<>(new FlightResponse(flightRepo.findById(fPk).get(), "Flight event appended!"),
				HttpStatus.OK);
	}

	public ResponseEntity<FlightResponse> healthErrorOneFlight(Exception e) {
		if (!hc.isHealthly(e)) {
			return new ResponseEntity<>(new FlightResponse(null, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
		}

		return new ResponseEntity<>(new FlightResponse(null, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@DeleteMapping("/{airportId}/{flightId}")
	@CircuitBreaker(name = Flight_CircuitBreaker, fallbackMethod = "healthErrorHttpStatus")
	public ResponseEntity<HttpStatus> delete(@PathVariable String airportId, @PathVariable UUID flightId) {
		FlightPk fPk = new FlightPk(airportId, flightId);
		flightRepo.deleteById(fPk);

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
