package com.bhatman.poc.astra.flight;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Flight {
    private Long flightId;
    private String flightName;
}
