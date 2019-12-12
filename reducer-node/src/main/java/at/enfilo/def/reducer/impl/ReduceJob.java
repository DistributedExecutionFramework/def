package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.node.routine.exec.SequenceStepsBuilder;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.reducer.server.Reducer;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.RoutineType;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

class ReduceJob implements Runnable {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReduceJob.class);

	private final static String KEY_REDUCE = "REDUCE";
	private final String jId;
	private final String reduceRoutineId;
	private final String storeRoutineId;
	private final RoutineProcessBuilderFactory routineProcessBuilderFactory;
	private final CountDownLatch waitLock;
	private final Object resourceLock;
	private int resourceCounter;
	private SequenceStepsExecutor sequenceStepsExecutor;
	private List<Result> results;
	private boolean running;
	private String error;

	public ReduceJob(String jId, String reduceRoutineId, String storeRoutineId) throws ClientCreationException {
		this(
				jId,
				reduceRoutineId,
				storeRoutineId,
				new RoutineProcessBuilderFactory(
						new LibraryServiceClientFactory().createClient(Reducer.getInstance().getConfiguration().getLibraryEndpoint()),
						Reducer.getInstance().getConfiguration()
				)
		);
	}

	public ReduceJob(String jId, String reduceRoutineId, String storeRoutineId, RoutineProcessBuilderFactory routineProcessBuilderFactory) {
		this.jId = jId;
		this.reduceRoutineId = reduceRoutineId;
		this.storeRoutineId = storeRoutineId;
		this.routineProcessBuilderFactory = routineProcessBuilderFactory;
		this.running = false;
		this.waitLock = new CountDownLatch(1);
		this.resourceLock = new Object();
	}


	@Override
	public void run() {
		try {
			LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Start reduce job with routine {}.", reduceRoutineId);

			ReducerConfiguration configuration = Reducer.getInstance().getConfiguration();
			List<SequenceStep> steps = new SequenceStepsBuilder(jId, configuration)
					.appendStep(reduceRoutineId, RoutineType.REDUCE)
					.appendStep(storeRoutineId, RoutineType.STORE)
					.getSequence();

			sequenceStepsExecutor = new SequenceStepsExecutor(
					jId,
					steps,
					new HashMap<>(), // Empty initial Parameters
					true,
					configuration,
					routineProcessBuilderFactory,
					DEFLoggerFactory.createJobContext(jId)
			);
			running = true;
			LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Reduce job is in state run.");
			results = sequenceStepsExecutor.run();
			running = false;
			LOGGER.info(DEFLoggerFactory.createJobContext(jId), "Reduce job finished.");

		} catch (InterruptedException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Reduce job interrupted.", e);
			error = e.getMessage();
			Thread.currentThread().interrupt();

		} catch (Exception e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Reduce job failed.", e);
			error = e.getMessage();

		} finally {
			waitLock.countDown();
		}
	}

	public void addResources(List<ResourceDTO> resources) {
		for (ResourceDTO resource : resources) {
			synchronized (resourceLock) {
				sequenceStepsExecutor.getCommunicator().addParameter(
						Integer.toString(resourceCounter),
						resource
				);
				resourceCounter++;
			}
		}
	}

	public void reduce() {
		ResourceDTO end = new ResourceDTO();
		end.setKey(KEY_REDUCE);
		end.setData(new byte[]{});
		synchronized (resourceLock) {
			sequenceStepsExecutor.getCommunicator().addParameter(
					Integer.toString(resourceCounter++),
					end
			);
		}
	}

	public List<Result> getResults() {
		return results;
	}

	public boolean isRunning() {
		return running;
	}

	public String getReduceRoutineId() {
		return reduceRoutineId;
	}

	public String getStoreRoutineId() {
		return storeRoutineId;
	}

	public String getError() {
		return error;
	}

	public boolean isSuccessful() {
		return error == null;
	}

	public void reduceAndWait() throws InterruptedException {
		reduce();
		waitLock.await();
	}
}
