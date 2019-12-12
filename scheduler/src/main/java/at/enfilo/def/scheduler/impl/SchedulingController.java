package at.enfilo.def.scheduler.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.api.NodeCommunicationException;
import at.enfilo.def.scheduler.api.ScheduleReduceException;
import at.enfilo.def.scheduler.api.ScheduleTaskException;
import at.enfilo.def.scheduler.api.strategy.IReduceSchedulingStrategy;
import at.enfilo.def.scheduler.api.strategy.ITaskSchedulingStrategy;
import at.enfilo.def.scheduler.server.Scheduler;
import at.enfilo.def.scheduler.strategy.*;
import at.enfilo.def.scheduler.util.SchedulerConfiguration;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class SchedulingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingController.class);

	private final ITaskSchedulingStrategy taskSchedulingStrategy;
	private final IReduceSchedulingStrategy reduceSchedulingStrategy;
	private final SchedulerConfiguration schedulerConfiguration;


	/**
	 * Private class to provide thread safe singleton
	 */
	private static class ThreadSafeLazySingletonWrapper {
		private static final SchedulingController INSTANCE = new SchedulingController(
			Scheduler.getInstance().getConfiguration()
		);

		private ThreadSafeLazySingletonWrapper() {}
	}

	/**
	 * Singleton pattern, returns instance.
	 *
	 * @return - singleton instance
	 */
	public static SchedulingController getInstance() {
		return ThreadSafeLazySingletonWrapper.INSTANCE;
	}

	/**
	 * Hide constructor
	 */
	private SchedulingController(SchedulerConfiguration schedulerConfiguration) {
		this.schedulerConfiguration = schedulerConfiguration;
		this.taskSchedulingStrategy = getTaskSchedulingStrategy();
		this.reduceSchedulingStrategy = getReduceSchedulingStrategy();
	}

	/**
	 * Try to start/instantiate real {@link ITaskSchedulingStrategy} implementation.
	 * @return real scheduler implementation
	 */
	private ITaskSchedulingStrategy getTaskSchedulingStrategy() {
		ITaskSchedulingStrategy strategy = null;
		try {
			// Loading strategy.
			final String schedulingStrategy = schedulerConfiguration.getTaskSchedulingStrategy();
			LOGGER.debug("Try to create task scheduling instance from \"{}\".", schedulingStrategy);

			Class<?> strategyCls = Class.forName(schedulingStrategy);
			strategy = strategyCls
					.asSubclass(TaskSchedulingStrategy.class)
					.getDeclaredConstructor(SchedulerConfiguration.class)
					.newInstance(schedulerConfiguration);

			LOGGER.info("Scheduling strategy {} successfully initialized", strategyCls);

		} catch (Exception e) {
			LOGGER.error("Error while create scheduling instance, fallback to default strategy: RoundRobin, Worker scenario.", e);

		} finally {
			// Fallback to RoundRobin scheduling strategy
			if (strategy == null) {
				strategy = new RoundRobinSchedulingStrategy(schedulerConfiguration);
			}
		}
		return strategy;
	}

	/**
	 * Try to start/instantiate real {@link IReduceSchedulingStrategy} implementation.
	 * @return real scheduler implementation
	 */
	private IReduceSchedulingStrategy getReduceSchedulingStrategy() {
		IReduceSchedulingStrategy strategy = null;
		try {
			// Loading strategy.
			final String schedulingStrategy = schedulerConfiguration.getReduceSchedulingStrategy();
			LOGGER.debug("Try to create reduce scheduling instance from \"{}\".", schedulingStrategy);

			Class<?> strategyCls = Class.forName(schedulingStrategy);
			strategy = strategyCls
					.asSubclass(ReduceSchedulingStrategy.class)
					.getDeclaredConstructor(SchedulerConfiguration.class)
					.newInstance(schedulerConfiguration);

			LOGGER.info("Scheduling strategy {} successfully initialized", strategyCls);

		} catch (Exception e) {
			LOGGER.error("Error while create scheduling instance, fallback to default strategy: RoundRobin, Worker scenario.", e);

		} finally {
			// Fallback to RoundRobin scheduling strategy
			if (strategy == null) {
				strategy = new DefaultReduceSchedulingStrategy(schedulerConfiguration);
			}
		}
		return strategy;
	}

	public void addWorker(String nodeId, ServiceEndpointDTO endpoint) throws NodeCommunicationException {
		taskSchedulingStrategy.addWorker(nodeId, endpoint);
	}

	public void removeWorker(String nodeId) {
		taskSchedulingStrategy.removeWorker(nodeId);
	}

	public void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) {
		taskSchedulingStrategy.notifyNodeInfo(nId, nodeInfo);
	}

	public void addJob(String jId) throws NodeCommunicationException {
		taskSchedulingStrategy.addJob(jId);
	}

	public void extendToReduceJob(String jId, String reduceRoutineId) throws ScheduleReduceException {
		reduceSchedulingStrategy.addJob(jId, reduceRoutineId);
	}

	public void scheduleTask(String jId, Collection<TaskDTO> tasks) throws ScheduleTaskException {
		taskSchedulingStrategy.schedule(jId, tasks);
	}

	public void markJobAsComplete(String jId) throws ScheduleTaskException {
		taskSchedulingStrategy.markJobAsComplete(jId);
	}

	public void removeJob(String jId) throws NodeCommunicationException, UnknownJobException, ScheduleReduceException {
		taskSchedulingStrategy.removeJob(jId);
		if (reduceSchedulingStrategy.getJobs().contains(jId)) {
			reduceSchedulingStrategy.deleteJob(jId);
		}
	}

	public List<ResourceDTO> finalizeReduce(String jId) throws ScheduleReduceException {
		return reduceSchedulingStrategy.finalizeReduce(jId);
	}

	public void addReducer(String nId, ServiceEndpointDTO endpoint) throws NodeCommunicationException {
		reduceSchedulingStrategy.addReducer(nId, endpoint);
	}

	public void removeReducer(String nId) {
		reduceSchedulingStrategy.removeReducer(nId);
	}

	public void scheduleReduce(String jId, List<ResourceDTO> resources) throws ScheduleReduceException {
		reduceSchedulingStrategy.scheduleReduce(jId, resources);
	}

	public void notifyTasksNewState(String nId, List<String> taskIds, ExecutionState newState) {
		taskSchedulingStrategy.notifyTasksNewState(nId, taskIds, newState);
		reduceSchedulingStrategy.notifyTasksNewState(nId, taskIds, newState);
	}
}
