package com.bhatman.poc.astra.flight;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface FlightMapper {

	@DaoFactory
	FlightDAO flightDao();
}
