package at.enfilo.def.node.impl;

import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.TSerializer;

import java.util.List;

public abstract class ExecutorService<T extends TBase> extends Thread {

    private final QueuePriorityWrapper<T> queuePriorityWrapper;
    private final IStateChangeListener stateChangeListener;
    private final Object runningLock;
    private final DTOCache<T> cache;

    protected boolean isActive;
    private boolean running;
    private SequenceStepsExecutor sequenceStepsExecutor;
    private String runningElement;

    private String storeRoutineId;

    private static final TFieldIdEnum ID_FIELD;

    static {
        ID_FIELD = new TFieldIdEnum() {
            @Override
            public short getThriftFieldId() {
                return 1;
            }

            @Override
            public String getFieldName() {
                return "_id";
            }
        };
    }


    public ExecutorService(
            QueuePriorityWrapper queuePriorityWrapper,
            String storeRoutineId,
            IStateChangeListener stateChangeListener,
            String dtoCacheContext,
            Class<T> cls
    ) {

        this.queuePriorityWrapper = queuePriorityWrapper;
        this.storeRoutineId = storeRoutineId;
        this.stateChangeListener = stateChangeListener;

        this.isActive = true;
        this.running = false;
        this.runningLock = new Object();
        this.cache = DTOCache.getInstance(dtoCacheContext, cls);
    }

    /**
     * For unit testing
     * @param queuePriorityWrapper
     * @param stateChangeListener
     * @param cache
     */
    protected ExecutorService(
            QueuePriorityWrapper<T> queuePriorityWrapper,
            String storeRoutineId,
            IStateChangeListener stateChangeListener,
            DTOCache<T> cache
    ) {
        this.queuePriorityWrapper = queuePriorityWrapper;
        this.storeRoutineId = storeRoutineId;
        this.stateChangeListener = stateChangeListener;

        this.isActive = true;
        this.running = false;
        this.runningLock = new Object();
        this.cache = cache;
    }

    @Override
    public void run() {
        logInfo("Start ExecutorService.");

        while (this.isActive) {
            try {
                // Fetch next element from queuePriorityWrapper and create a running sequence
                T element = this.queuePriorityWrapper.enqueue();

                // Run (execute) an element
                run(element);
            } catch (Exception e) {
                logError("Error while running ExecutorService.", e);
                this.isActive = false;
            }
        }

        logInfo("ExecutorService terminated.");
    }

    /**
     * Runs / executes an element.
     *
     * @param element - to execute
     */
    protected void run(T element) {
        synchronized (this.runningLock) {
            this.runningElement = getElementId(element);
        }

        // Change element to state RUN
        prepareElementForExecution(element);
        this.cache.cache(getElementId(element), element);
        stateChangeListener.notifyStateChanged(getElementId(element), ExecutionState.SCHEDULED, ExecutionState.RUN);

        logInfo(element, String.format("Proceed next element on %s.", this.getName()));

        try {
            // Create sequence steps executor
            this.sequenceStepsExecutor = buildSequenceStepsExecutor(element);

            // Run Element --> All routines as processes
            this.running = true;
            List<Result> results = executeElement(element, this.sequenceStepsExecutor);
            this.running = false;
            this.sequenceStepsExecutor = null;

            handleSuccessfulExecutionOfElement(element, results);
        } catch (Exception e) {
            // Element execution failed
            logError(element, "Failed to execute element: " + e.getMessage(), e);
            handleFailedExecutionOfElement(element, e);
        } finally {
            this.cache.cache(getElementId(element), element); // Update cache

            synchronized (this.runningLock) {
                this.runningElement = null;
            }

            logInfo(element, String.format("Element finished with state %s.", getElementState(element)));

            // Notify element is finished
            this.stateChangeListener.notifyStateChanged(getElementId(element), ExecutionState.RUN, getElementState(element));
        }
    }

    protected abstract void logInfo(String message);
    protected abstract void logInfo(T element, String message);
    protected abstract void logError(String message, Exception e);
    protected abstract void logError(T element, String message, Exception e);

    protected abstract String getElementId(T element);
    protected abstract ExecutionState getElementState(T element);

    protected abstract SequenceStepsExecutor buildSequenceStepsExecutor(T element) throws QueueNotExistsException;

    protected abstract void prepareElementForExecution(T element);

    protected abstract List<Result> executeElement(T element, SequenceStepsExecutor executor) throws Exception;

    protected abstract void handleSuccessfulExecutionOfElement(T element, List<Result> results) throws Exception;
    protected abstract void handleFailedExecutionOfElement(T element, Exception e);

    /**
     * Cancel current running element.
     */
    public void cancelRunningElement() {
        if (this.sequenceStepsExecutor != null) {
            this.sequenceStepsExecutor.cancel();
        }
    }

    /**
     * Return the id of the current running element.
     *
     * @return id of the current running element as {@link String}
     */
    public String getRunningElement() {
        synchronized (this.runningLock) {
            return this.runningElement;
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public String getStoreRoutineId() { return this.storeRoutineId; }

    public void setStoreRoutineId(String storeRoutineId) { this.storeRoutineId = storeRoutineId; }

    public void shutdown() {
        logInfo("Shutdown command received.");

        if (this.sequenceStepsExecutor != null && this.sequenceStepsExecutor.isRunning()) {
            this.sequenceStepsExecutor.cancel();
        }
        this.isActive = false;
    }

    protected ResourceDTO createResourceDTO(TBase value) throws TException {
        TSerializer serializer = new TSerializer();
        ResourceDTO resource = new ResourceDTO();
        TFieldIdEnum field = value.fieldForId(ID_FIELD.getThriftFieldId());
        if (field.getFieldName().equals(ID_FIELD.getFieldName())) {
            String dataTypeId = value.getFieldValue(field).toString();
            resource.setDataTypeId(dataTypeId);
        }
        resource.setData(serializer.serialize(value));
        return resource;
    }

}
