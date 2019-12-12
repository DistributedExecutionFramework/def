package at.enfilo.def.manager.webservice.mocks;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.library.api.UnknownDataTypeException;
import at.enfilo.def.library.api.util.BaseDataTypeRegistry;
import at.enfilo.def.manager.webservice.impl.JobServiceImpl;
import at.enfilo.def.manager.webservice.util.DataConverter;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JobServiceImplMock extends JobServiceImpl {

    public Future<JobDTO> jobDTOFuture;
    private JobDTO job;
    public String resourceId;
    public Future<List<String>> jobIdsFuture;
    public List<String> jobIds;
    public Future<String> routineIdFuture;
    public Future<Void> doneTicketStatusFuture;

    public JobServiceImplMock() {
        super();
        initMockingComponents();
    }

    private void initMockingComponents() {

        try {
            job = new JobDTO();
            job.setId(UUID.randomUUID().toString());
            job.setState(ExecutionState.SUCCESS);
            ResourceDTO reducedResult = new ResourceDTO();
            resourceId = UUID.randomUUID().toString();
            reducedResult.setId(resourceId);
            reducedResult.setKey("DEFAULT");
            reducedResult.setDataTypeId(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble").getId());
            DEFDouble defDouble = new DEFDouble(4.5);
            TSerializer serializer = new TSerializer();
            reducedResult.setData(serializer.serialize(defDouble));
            job.addToReducedResults(reducedResult);
        } catch (UnknownDataTypeException | TException e) {
            e.printStackTrace();
        }

        jobIds = new LinkedList<>();
        jobIds.add(job.getId());

        // create mocking class and mock methods
        serviceClient = Mockito.mock(IExecLogicServiceClient.class);
        dataConverter = Mockito.mock(DataConverter.class);

        jobDTOFuture = new Future<JobDTO>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public JobDTO get() throws InterruptedException, ExecutionException {
                return job;
            }

            @Override
            public JobDTO get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        routineIdFuture = new Future<String>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public String get() throws InterruptedException, ExecutionException {
                return UUID.randomUUID().toString();
            }

            @Override
            public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        doneTicketStatusFuture = new Future<Void>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                return null;
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        jobIdsFuture = new Future<List<String>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public List<String> get() throws InterruptedException, ExecutionException {
                return jobIds;
            }

            @Override
            public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
    }
}
