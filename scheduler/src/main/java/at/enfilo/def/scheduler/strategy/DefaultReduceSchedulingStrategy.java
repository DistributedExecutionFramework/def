package at.enfilo.def.scheduler.strategy;

import at.enfilo.def.scheduler.util.SchedulerConfiguration;

import java.util.List;

public class DefaultReduceSchedulingStrategy extends ReduceSchedulingStrategy {

	/**
	 * Constructor for implementation.
	 *
	 * @param schedulerConfiguration
	 */
	public DefaultReduceSchedulingStrategy(SchedulerConfiguration schedulerConfiguration) {
		super(schedulerConfiguration);
	}

	protected String map(String key, List<String> nodes) {
		int index = Math.abs(key.hashCode() % nodes.size());
		return nodes.get(index);
	}
}
