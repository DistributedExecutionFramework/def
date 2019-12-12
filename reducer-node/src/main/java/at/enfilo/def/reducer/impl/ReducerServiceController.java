package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.node.api.NodeServiceClientFactory;
import at.enfilo.def.node.impl.NodeServiceController;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.util.ResultUtil;
import at.enfilo.def.reducer.server.Reducer;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReducerServiceController extends NodeServiceController {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReducerServiceController.class);

	/**
     * Private class to provide thread safe singleton.
     */
    private static class ThreadSafeLazySingletonWrapper {
        private static final ReducerServiceController INSTANCE = new ReducerServiceController();

        private ThreadSafeLazySingletonWrapper() {}
    }

    private final ExecutorService executorService;
	private final Map<String, ReduceJob> reduceJobs;
	private final Map<String, List<ResourceDTO>> results;
	private final Map<String, Future<?>> reduceJobFutures;
	private final ReducerConfiguration configuration;
    private String storeRoutineId;

    private ReducerServiceController() {
        this(
            new LinkedList<>(),
            new NodeServiceClientFactory(),
            Reducer.getInstance().getConfiguration(),
			new HashMap<>(),
			new HashMap<>(),
			Executors.newFixedThreadPool(Reducer.getInstance().getConfiguration().getExecutionThreads())
        );
    }

    private ReducerServiceController(
        List<INodeObserverServiceClient> observers,
        UnifiedClientFactory<? extends INodeServiceClient> nodeServiceClientFactory,
        ReducerConfiguration reducerConfiguration,
		Map<String, ReduceJob> reduceJobs,
		Map<String, List<ResourceDTO>> results,
		ExecutorService executorService
    ) {
        // Should be implemented as thread safe lazy singleton.
        super(
			NodeType.REDUCER,
            observers,
			reducerConfiguration,
			new NodeObserverServiceClientFactory(),
            LOGGER
        );

        this.reduceJobs = reduceJobs;
        this.results = results;
        this.configuration = reducerConfiguration;
        this.storeRoutineId = reducerConfiguration.getStoreRoutineId();
        this.executorService = executorService;
        this.reduceJobFutures = new HashMap<>();
    }

    /**
     * Singleton pattern.
     * @return a instance of {@link ReducerServiceController}
     */
    public static ReducerServiceController getInstance() {
        return ThreadSafeLazySingletonWrapper.INSTANCE;
    }

    public String getStoreRoutineId() {
        return storeRoutineId;
    }

    public void setStoreRoutineId(String storeRoutineId) {
        this.storeRoutineId = storeRoutineId;
    }

	public void createReduceJob(String jId, String routineId) throws ClientCreationException {
    	LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Try to create and start a ReduceJob with Routine {}.", routineId);
		try {
			ReduceJob reduceJob = new ReduceJob(jId, routineId, storeRoutineId);
			Future<?> future = executorService.submit(reduceJob);
			reduceJobs.put(jId, reduceJob);
			reduceJobFutures.put(jId, future);
			LOGGER.info(DEFLoggerFactory.createJobContext(jId), "Successful started ReduceJob with Routine {}.", routineId);

		} catch (ClientCreationException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while create reduce job.", e);
			throw e;
		}
	}

	public void deleteReduceJob(String jId) throws UnknownJobException {
		if (reduceJobs.containsKey(jId)) {
			LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Try to abort and delete ReduceJob.");
			Future<?> future = reduceJobFutures.get(jId);
			future.cancel(true);
			reduceJobFutures.remove(jId);
			reduceJobs.remove(jId);
			results.remove(jId);
			LOGGER.info(DEFLoggerFactory.createJobContext(jId), "ReduceJob aborted and deleted.");
		} else {
			String msg = String.format("Cannot delete an unknown ReduceJob: %s.", jId);
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), msg);
			throw new UnknownJobException(msg);
		}
	}

	public void addResources(String jId, List<ResourceDTO> resources) throws UnknownJobException {
    	if (reduceJobs.containsKey(jId)) {
    		reduceJobs.get(jId).addResources(resources);
    		LOGGER.info(DEFLoggerFactory.createJobContext(jId), "Added resource to reduce.");
		} else {
    		String msg = String.format("Reduce job %s not known.", jId);
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), msg);
    		throw new UnknownJobException(msg);
		}
	}

	public void reduce(String jId) throws UnknownJobException, ReduceJobException {
		if (reduceJobs.containsKey(jId)) {
			try {
				// reduce and wait
				ReduceJob reduceJob = reduceJobs.get(jId);
				reduceJob.reduceAndWait();

				// extract results
				if (reduceJob.isSuccessful()) {
					results.put(jId, new LinkedList<>());
					for (Result r : reduceJob.getResults()) {
						// TODO: create a helper class for this (with drivers for different persistence types)
						ResourceDTO resource = new ResourceDTO();
						resource.setId(UUID.randomUUID().toString());
						resource.setData(r.getData());
						resource.setKey(r.getKey());
						resource.setDataTypeId(ResultUtil.extractDataTypeId(r));
						results.get(jId).add(resource);
					}
				} else {
					throw new ReduceJobException(String.format("Reduce job failed: %s.", reduceJob.getError()));
				}

			} catch (InterruptedException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Reduce interrupted.", e);
				Thread.currentThread().interrupt();
			} catch (ReduceJobException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Reduce failed.", e);
				throw e;
			} finally {
				reduceJobs.remove(jId);
				reduceJobFutures.remove(jId);
			}
		} else {
			String msg = String.format("Reduce job %s not known.", jId);
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), msg);
			throw new UnknownJobException(msg);
		}

	}

	public List<ResourceDTO> fetchResult(String jId) throws UnknownJobException {
    	if (results.containsKey(jId)) {
    		return results.remove(jId);
		} else {
			String msg = String.format("Reduce job %s not known.", jId);
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), msg);
			throw new UnknownJobException(msg);
		}
	}

	@Override
	protected Map<String, String> getNodeInfoParameters() {
		return new HashMap<>();
	}
}
