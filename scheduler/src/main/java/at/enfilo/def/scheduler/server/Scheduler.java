package at.enfilo.def.scheduler.server;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;
import at.enfilo.def.scheduler.clientroutineworker.api.strategy.IProgramSchedulingStrategy;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerResponseService;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerService;
import at.enfilo.def.scheduler.clientroutineworker.impl.ClientRoutineWorkerSchedulerResponseServiceImpl;
import at.enfilo.def.scheduler.clientroutineworker.impl.ClientRoutineWorkerSchedulerServiceImpl;
import at.enfilo.def.scheduler.clientroutineworker.strategy.ProgramSchedulingStrategy;
import at.enfilo.def.scheduler.clientroutineworker.strategy.RoundRobinProgramSchedulingStrategy;
import at.enfilo.def.scheduler.general.impl.NodeObserverServiceImpl;
import at.enfilo.def.scheduler.reducer.api.strategy.IReduceSchedulingStrategy;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerResponseService;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerService;
import at.enfilo.def.scheduler.reducer.impl.ReducerSchedulerResponseServiceImpl;
import at.enfilo.def.scheduler.reducer.impl.ReducerSchedulerServiceImpl;
import at.enfilo.def.scheduler.reducer.strategy.DefaultReduceSchedulingStrategy;
import at.enfilo.def.scheduler.reducer.strategy.ReduceSchedulingStrategy;
import at.enfilo.def.scheduler.general.util.SchedulerConfiguration;
import at.enfilo.def.scheduler.worker.api.strategy.ITaskSchedulingStrategy;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerResponseService;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerService;
import at.enfilo.def.scheduler.worker.impl.WorkerSchedulerResponseServiceImpl;
import at.enfilo.def.scheduler.worker.impl.WorkerSchedulerServiceImpl;
import at.enfilo.def.scheduler.worker.strategy.RoundRobinTaskSchedulingStrategy;
import at.enfilo.def.scheduler.worker.strategy.TaskSchedulingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class Scheduler extends ServerStartup<SchedulerConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
	private static final String CONFIG_FILE = "scheduler.yml";

	private static Scheduler instance;

	public Scheduler() {
		super(Scheduler.class, SchedulerConfiguration.class, CONFIG_FILE, LOGGER);
	}

	public static void main(String[] args) {
		LOGGER.info("Startup Scheduler");

		getInstance().startServices();
	}

	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		ITaskSchedulingStrategy taskSchedulingStrategy = getTaskSchedulingStrategy();
		IReduceSchedulingStrategy reduceSchedulingStrategy = getReduceSchedulingStrategy();
		IProgramSchedulingStrategy programSchedulingStrategy = getProgramSchedulingStrategy();

		ThriftProcessor<WorkerSchedulerServiceImpl> workerSchedulerServiceProcessor = new ThriftProcessor<>(
			WorkerSchedulerService.class.getName(),
			new WorkerSchedulerServiceImpl(taskSchedulingStrategy),
			WorkerSchedulerService.Processor<WorkerSchedulerService.Iface>::new
		);

		ThriftProcessor<WorkerSchedulerResponseServiceImpl> workerSchedulerResponseServiceProcessor = new ThriftProcessor<>(
			WorkerSchedulerResponseService.class.getName(),
			new WorkerSchedulerResponseServiceImpl(),
			WorkerSchedulerResponseService.Processor<WorkerSchedulerResponseService.Iface>::new
		);

		ThriftProcessor<ReducerSchedulerServiceImpl> reducerSchedulerServiceProcessor = new ThriftProcessor<>(
			ReducerSchedulerService.class.getName(),
			new ReducerSchedulerServiceImpl(reduceSchedulingStrategy),
			ReducerSchedulerService.Processor<ReducerSchedulerService.Iface>::new
		);

		ThriftProcessor<ReducerSchedulerResponseServiceImpl> reducerSchedulerResponseServiceProcessor = new ThriftProcessor<>(
			ReducerSchedulerResponseService.class.getName(),
			new ReducerSchedulerResponseServiceImpl(),
			ReducerSchedulerResponseService.Processor<ReducerSchedulerResponseService.Iface>::new
		);

		ThriftProcessor<ClientRoutineWorkerSchedulerServiceImpl> clientRoutineWorkerSchedulerServiceProcessor = new ThriftProcessor<>(
				ClientRoutineWorkerSchedulerService.class.getName(),
				new ClientRoutineWorkerSchedulerServiceImpl(programSchedulingStrategy),
				ClientRoutineWorkerSchedulerService.Processor<ClientRoutineWorkerSchedulerService.Iface>::new
		);

		ThriftProcessor<ClientRoutineWorkerSchedulerResponseServiceImpl> clientRoutineWorkerSchedulerResponseServiceProcessor = new ThriftProcessor<>(
				ClientRoutineWorkerSchedulerResponseService.class.getName(),
				new ClientRoutineWorkerSchedulerResponseServiceImpl(),
				ClientRoutineWorkerSchedulerResponseService.Processor<ClientRoutineWorkerSchedulerResponseService.Iface>::new
		);

		ThriftProcessor<NodeObserverServiceImpl> observerServiceProcessor = new ThriftProcessor<>(
			NodeObserverService.class.getName(),
			new NodeObserverServiceImpl(taskSchedulingStrategy, reduceSchedulingStrategy),
			NodeObserverService.Processor<NodeObserverService.Iface>::new
		);

		List<ThriftProcessor> processors = new LinkedList<>();
		processors.add(workerSchedulerServiceProcessor);
		processors.add(workerSchedulerResponseServiceProcessor);
		processors.add(reducerSchedulerServiceProcessor);
		processors.add(reducerSchedulerResponseServiceProcessor);
		processors.add(clientRoutineWorkerSchedulerServiceProcessor);
		processors.add(clientRoutineWorkerSchedulerResponseServiceProcessor);
		processors.add(observerServiceProcessor);
		return processors;
	}

	@Override
	protected List<IResource> getWebResources() {
		ITaskSchedulingStrategy taskSchedulingStrategy = getTaskSchedulingStrategy();
		IReduceSchedulingStrategy reduceSchedulingStrategy = getReduceSchedulingStrategy();
		IProgramSchedulingStrategy programSchedulingStrategy = getProgramSchedulingStrategy();

		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(new WorkerSchedulerServiceImpl(taskSchedulingStrategy));
		resourceList.add(new WorkerSchedulerResponseServiceImpl());
		resourceList.add(new ReducerSchedulerServiceImpl(reduceSchedulingStrategy));
		resourceList.add(new ReducerSchedulerResponseServiceImpl());
		resourceList.add(new ClientRoutineWorkerSchedulerServiceImpl(programSchedulingStrategy));
		resourceList.add(new ClientRoutineWorkerSchedulerResponseServiceImpl());
		resourceList.add(new NodeObserverServiceImpl(taskSchedulingStrategy, reduceSchedulingStrategy));
		return resourceList;
	}

	public static Scheduler getInstance() {
		if (instance == null) {
			instance = new Scheduler();
		}
		return instance;
	}


	/**
	 * Try to start/instantiate real {@link ITaskSchedulingStrategy} implementation.
	 * @return real scheduler implementation
	 */
	private ITaskSchedulingStrategy getTaskSchedulingStrategy() {
		SchedulerConfiguration configuration = getConfiguration();
		ITaskSchedulingStrategy strategy = null;
		try {
			// Loading strategy.
			final String schedulingStrategy = configuration.getTaskSchedulingStrategy();
			LOGGER.debug("Try to create task scheduling instance from \"{}\".", schedulingStrategy);

			Class<?> strategyCls = Class.forName(schedulingStrategy);
			strategy = strategyCls
					.asSubclass(TaskSchedulingStrategy.class)
					.getDeclaredConstructor(SchedulerConfiguration.class)
					.newInstance(configuration);

			LOGGER.info("Scheduling strategy {} successfully initialized", strategyCls);

		} catch (Exception e) {
			LOGGER.error("Error while create scheduling instance, fallback to default strategy: RoundRobin, Worker scenario.", e);

		} finally {
			// Fallback to RoundRobin scheduling strategy
			if (strategy == null) {
				strategy = new RoundRobinTaskSchedulingStrategy(configuration);
			}
		}
		return strategy;
	}

	/**
	 * Try to start/instantiate real {@link IReduceSchedulingStrategy} implementation.
	 * @return real scheduler implementation
	 */
	private IReduceSchedulingStrategy getReduceSchedulingStrategy() {
		SchedulerConfiguration configuration = getConfiguration();
		IReduceSchedulingStrategy strategy = null;
		try {
			// Loading strategy.
			final String schedulingStrategy = configuration.getReduceSchedulingStrategy();
			LOGGER.debug("Try to create reduce scheduling instance from \"{}\".", schedulingStrategy);

			Class<?> strategyCls = Class.forName(schedulingStrategy);
			strategy = strategyCls
					.asSubclass(ReduceSchedulingStrategy.class)
					.getDeclaredConstructor(SchedulerConfiguration.class)
					.newInstance(configuration);

			LOGGER.info("Scheduling strategy {} successfully initialized", strategyCls);

		} catch (Exception e) {
			LOGGER.error("Error while create scheduling instance, fallback to default strategy: RoundRobin, Worker scenario.", e);

		} finally {
			// Fallback to RoundRobin scheduling strategy
			if (strategy == null) {
				strategy = new DefaultReduceSchedulingStrategy(configuration);
			}
		}
		return strategy;
	}

	private IProgramSchedulingStrategy getProgramSchedulingStrategy() {
		SchedulerConfiguration configuration = getConfiguration();
		IProgramSchedulingStrategy strategy = null;
		try {
			// Loading strategy.
			final String schedulingStrategy = configuration.getProgramSchedulingStrategy();
			LOGGER.debug("Try to create program scheduling instance from \"{}\".", schedulingStrategy);

			Class<?> strategyCls = Class.forName(schedulingStrategy);
			strategy = strategyCls
					.asSubclass(ProgramSchedulingStrategy.class)
					.getDeclaredConstructor(SchedulerConfiguration.class)
					.newInstance(configuration);

			LOGGER.info("Scheduling strategy {} successfully initialized", strategyCls);
		} catch (Exception e) {
			LOGGER.error("Error while creating scheduling instance, fallback to default strategy: RoundRobin, Client routine worker scenario.", e);

		} finally {
			// Fallback to RoundRobin scheduling strategy
			if (strategy == null) {
				strategy = new RoundRobinProgramSchedulingStrategy(configuration);
			}
		}
		return strategy;
	}
}
