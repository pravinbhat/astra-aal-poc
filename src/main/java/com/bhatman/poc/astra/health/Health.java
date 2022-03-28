package com.bhatman.poc.astra.health;

import lombok.Data;

@Data
public class Health {
	public String name;
	public String status;
	public String cloudProvider;
	public String region;
}
