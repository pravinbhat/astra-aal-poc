package com.bhatman.poc.astra.flight;

import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@UserDefinedType("sys_local_hist") 
public class SysLocalHist {
	String syshealth;
}
