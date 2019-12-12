package at.enfilo.def.manager.webservice.mocks;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.library.api.UnknownDataTypeException;
import at.enfilo.def.library.api.util.BaseDataTypeRegistry;
import at.enfilo.def.manager.webservice.impl.TaskServiceImpl;
import at.enfilo.def.manager.webservice.util.DataConverter;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskServiceImplMock extends TaskServiceImpl {

    public Future<TaskDTO> taskDTOFuture;
    private TaskDTO task;
    public String inParameterName;
    private ResourceDTO inParameter;
    public ResourceDTO outParameter;
    public Future<Void> doneTicketStatusFuture;
    public List<String> taskIds;
    public Future<List<String>> taskIdsFuture;

    public TaskServiceImplMock() {
        super();
        initMockingComponents();
    }

    private void initMockingComponents() {

        try {
            TSerializer serializer = new TSerializer();

            this.inParameterName = "start";
            this.inParameter = new ResourceDTO();
            this.inParameter.setKey("DEFAULT");
            this.inParameter.setId(UUID.randomUUID().toString());
            this.inParameter.setDataTypeId(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble").getId());
            DEFDouble defDouble1 = new DEFDouble(4.5);
            this.inParameter.setData(serializer.serialize(defDouble1));

            this.outParameter = new ResourceDTO();
            this.outParameter.setKey("DEFAULT");
            this.outParameter.setId(UUID.randomUUID().toString());
            this.outParameter.setDataTypeId(BaseDataTypeRegistry.getInstance().getDataTypeByName("DEFDouble").getId());
            DEFDouble defDouble2 = new DEFDouble(4.0);
            this.outParameter.setData(serializer.serialize(defDouble2));

            this.task = new TaskDTO();
            this.task.setId(UUID.randomUUID().toString());
            Map<String, ResourceDTO> inParameters = new HashMap<>();
            inParameters = new HashMap<>();
            inParameters.put(this.inParameterName, this.inParameter);
            this.task.setInParameters(inParameters);
            this.task.addToOutParameters(this.outParameter);
        } catch (UnknownDataTypeException | TException e) {
            e.printStackTrace();
        }

        taskIds = new LinkedList<>();
        taskIds.add(UUID.randomUUID().toString());
        taskIds.add(UUID.randomUUID().toString());
        taskIds.add(UUID.randomUUID().toString());

        // create mocking classes
        serviceClient = Mockito.mock(IExecLogicServiceClient.class);
        dataConverter = Mockito.mock(DataConverter.class);

        taskIdsFuture = new Future<List<String>>() {
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
                return taskIds;
            }

            @Override
            public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        this.taskDTOFuture = new Future<TaskDTO>() {
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
            public TaskDTO get() throws InterruptedException, ExecutionException {
                return task;
            }

            @Override
            public TaskDTO get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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
    }
}
