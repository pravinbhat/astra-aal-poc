package com.bhatman.poc.astra.health;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

import lombok.AllArgsConstructor;
import lombok.Data;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

	@Autowired
	MetricRegistry registry;

	@GetMapping
	public ResponseEntity<List<SMetric>> get() {
		return new ResponseEntity<>(getMetrics(), HttpStatus.OK);
	}

	@GetMapping("/node")
	public ResponseEntity<List<SMetric>> getNode() {
		List<SMetric> vals = getMetrics().stream().filter(sm -> sm.getMetricName().contains(".nodes."))
				.collect(Collectors.toList());

		return new ResponseEntity<>(vals, HttpStatus.OK);
	}

	@GetMapping("/session")
	public ResponseEntity<List<SMetric>> getSession() {
		List<SMetric> vals = getMetrics().stream().filter(sm -> !sm.getMetricName().contains(".nodes."))
				.collect(Collectors.toList());
		return new ResponseEntity<>(vals, HttpStatus.OK);
	}

	private List<SMetric> getMetrics() {
		Map<String, Metric> metricsMap = registry.getMetrics();
		return metricsMap.entrySet().stream().map(val -> new SMetric(val.getKey(), val.getValue()))
				.collect(Collectors.toList());
	}

	@AllArgsConstructor
	@Data
	class SMetric {
		String metricName;
		Metric metricValue;
	}

}
