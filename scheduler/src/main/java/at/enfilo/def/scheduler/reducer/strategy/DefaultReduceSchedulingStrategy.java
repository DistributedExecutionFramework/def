package at.enfilo.def.scheduler.reducer.strategy;

import at.enfilo.def.scheduler.general.util.SchedulerConfiguration;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

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

	@Override
	public void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) {

	}
}
