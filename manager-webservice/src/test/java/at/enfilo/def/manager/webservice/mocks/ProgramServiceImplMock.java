package at.enfilo.def.manager.webservice.mocks;

import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.webservice.impl.ProgramServiceImpl;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProgramServiceImplMock extends ProgramServiceImpl {

    public List<String> ids;
    public Future<List<String>> idsFuture;
    public ProgramDTO programDTO;
    public Future<ProgramDTO> programDTOFuture;
    public Future<Void> doneTicketStatusFuture;

    public ProgramServiceImplMock() {
        super();
        initMockingComponents();
    }

    private void initMockingComponents() {

        ids = new LinkedList<>();
        ids.add(UUID.randomUUID().toString());
        ids.add(UUID.randomUUID().toString());
        ids.add(UUID.randomUUID().toString());

        programDTO = new ProgramDTO();
        programDTO.setName("Name");
        programDTO.setDescription("Description");

        serviceClient = Mockito.mock(IExecLogicServiceClient.class);

        idsFuture = new Future<List<String>>() {
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
                return ids;
            }

            @Override
            public List<String> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };

        programDTOFuture = new Future<ProgramDTO>() {
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
            public ProgramDTO get() throws InterruptedException, ExecutionException {
                return programDTO;
            }

            @Override
            public ProgramDTO get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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
