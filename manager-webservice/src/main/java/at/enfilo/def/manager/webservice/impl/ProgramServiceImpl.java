package at.enfilo.def.manager.webservice.impl;

import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.webservice.rest.IProgramService;
import at.enfilo.def.manager.webservice.server.ManagerWebservice;
import at.enfilo.def.manager.webservice.util.ManagerWebserviceConfiguration;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProgramServiceImpl implements IProgramService {

    /**
     * Logger for logging activities of instances of this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramServiceImpl.class);

    private final ManagerWebserviceConfiguration configuration;
    /**
     * IExecLogicServiceClient needed for accessing the execution logic implementations regarding programs
     */
    protected IExecLogicServiceClient serviceClient;

    /**
     * Constructor of ProgramServiceImpl
     */
    public ProgramServiceImpl() {
        configuration = ManagerWebservice.getInstance().getConfiguration();
        init();
    }

    /**
     * Initializing of all needed components
     */
    private void init() {

        // service client for execution logic is fetched
        try {
            LOGGER.info("Initialization of all needed components for managing programs");
            serviceClient = new ExecLogicServiceClientFactory().createClient(configuration.getExecLogicEndpoint());
        } catch (ClientCreationException e) {
            LOGGER.error("Error initializing ExecLogicServiceClient", e);
        }
    }

    @Override
    public List<String> getAllProgramIds(String userId) {
        try {
            LOGGER.info("Fetching all program ids");
            Future<List<String>> future = serviceClient.getAllPrograms(userId);
            return future.get();
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching all program ids", e);
            return Collections.emptyList();
        }
    }


    @Override
    public List<ProgramDTO> getAllPrograms(String userId) {
            List<String> programIds = getAllProgramIds(userId);
            LOGGER.info("Fetching all programs");
            List<ProgramDTO> programs = new LinkedList<>();
            for (String id : programIds) {
                try {
                Future<ProgramDTO> programDTOFuture = serviceClient.getProgram(id);
                ProgramDTO program = programDTOFuture.get();
                programs.add(program);
                } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
                    LOGGER.error("Error fetching program with id {}.", id, e);
                }
            }
            return programs;
    }


    @Override
    public ProgramDTO getProgramInfo(String pId) {
        try {
            LOGGER.info("Fetching program info of program with id '" + pId + "'");
            Future<ProgramDTO> future = serviceClient.getProgram(pId);
            ProgramDTO program = future.get();
            return program;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error fetching program info of program with id '" + pId + "'", e);
            return null;
        }
    }


    @Override
    public TicketStatusDTO deleteProgram(String pId) {
        try {
            LOGGER.info("Deleting program with id '" + pId + "'");
            Future<Void> future = serviceClient.deleteProgram(pId);
            future.get();
            return TicketStatusDTO.DONE;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error deleting program with id '" + pId + "'", e);
            return TicketStatusDTO.FAILED;
        }
    }

    @Override
    public TicketStatusDTO abortProgram(String pId) {
        try {
            LOGGER.info("Aborting program with id '" + pId + "'");
            Future<Void> future = serviceClient.abortProgram(pId);
            future.get();
            return TicketStatusDTO.DONE;
        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error aborting program with id {}", pId, e);
            return TicketStatusDTO.FAILED;
        }
    }

    @Override
    public TicketStatusDTO updateProgram(String pId, ProgramDTO program) {
        try {
            Future<Void> updateNameFuture;
            if (program.getName() != null) {
                updateNameFuture = serviceClient.updateProgramName(pId, program.getName());
                updateNameFuture.get();
            }
            Future<Void> updateDescriptionFuture;
            if (program.getDescription() != null) {
                updateDescriptionFuture = serviceClient.updateProgramDescription(pId, program.getDescription());
                updateDescriptionFuture.get();
            }

            return TicketStatusDTO.DONE;

        } catch (InterruptedException | ExecutionException | ClientCommunicationException e) {
            LOGGER.error("Error updating program with id {}", pId, e);
            return TicketStatusDTO.FAILED;
        }
    }
}
