package com.bhatman.poc.astra.flight;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Update;

@Dao
public interface FlightDAO {

	@Update
    void update(FlightEntity flight);
}
