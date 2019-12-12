package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.util.configuration.ClusterConfiguration;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownResourceException;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class DomainControllerTest {

    private DomainController domainController;
    private Map<String, Program> programMap;
    private ClusterConfiguration clusterConfiguration;
    private Random rnd;
    private DTOCache<TaskDTO> taskCache;

    @Before
    public void setUp() throws Exception {
        programMap = new TimeoutMap<>(
                1, TimeUnit.HOURS,
                1, TimeUnit.HOURS
        );
        rnd = new Random();
        taskCache = DTOCache.getInstance(DomainController.DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
        clusterConfiguration = Mockito.mock(ClusterConfiguration.class);

        // Create controller with special constructor
        Constructor<DomainController> constructor = DomainController.class.getDeclaredConstructor(
                ClusterConfiguration.class,
                Map.class,
                DTOCache.class
        );
        constructor.setAccessible(true);
        domainController = constructor.newInstance(
                clusterConfiguration,
                programMap,
                taskCache
        );
    }

    @Test
    public void getAllProgramsOfUser() {
        // Create users
        String user1Id = "user1";

        String user2Id = "user2";

        // Create programs
        Program p1 = new Program(user1Id);
        String p1Id = p1.getId();

        Program p2 = new Program(user1Id);
        String p2Id = p2.getId();

        Program p3 = new Program(user2Id);
        String p3Id = p3.getId();

        programMap.put(p1Id, p1);
        programMap.put(p2Id, p2);
        programMap.put(p3Id, p3);

        assertEquals(2, domainController.getAllPrograms(user1Id).size());
        assertEquals(1, domainController.getAllPrograms(user2Id).size());
        assertTrue(domainController.getAllPrograms(user1Id).contains(p1Id));
        assertTrue(domainController.getAllPrograms(user1Id).contains(p2Id));
        assertTrue(domainController.getAllPrograms(user2Id).contains(p3Id));
    }

    @Test
    public void getAllProgramIds() {
        Program p1 = new Program("user1");
        String p1Id = p1.getId();

        Program p2 = new Program("user1");
        String p2Id = p2.getId();

        Program p3 = new Program("user1");
        String p3Id = p3.getId();

        programMap.put(p1Id, p1);
        programMap.put(p2Id, p2);
        programMap.put(p3Id, p3);

        assertEquals(3, domainController.getAllProgramIds().size());
        assertTrue(domainController.getAllProgramIds().contains(p1Id));
        assertTrue(domainController.getAllProgramIds().contains(p2Id));
        assertTrue(domainController.getAllProgramIds().contains(p3Id));
    }

    @Test
    public void getAllPrograms() {
        Program p1 = new Program("user1");
        String p1Id = p1.getId();

        Program p2 = new Program("user1");
        String p2Id = p2.getId();

        Program p3 = new Program("user1");
        String p3Id = p3.getId();

        programMap.put(p1Id, p1);
        programMap.put(p2Id, p2);
        programMap.put(p3Id, p3);

        assertEquals(3, domainController.getAllPrograms().size());
        assertTrue(domainController.getAllPrograms().stream().map(ProgramDTO::getId).collect(Collectors.toList()).contains(p1Id));
        assertTrue(domainController.getAllPrograms().stream().map(ProgramDTO::getId).collect(Collectors.toList()).contains(p2Id));
        assertTrue(domainController.getAllPrograms().stream().map(ProgramDTO::getId).collect(Collectors.toList()).contains(p3Id));
    }

    @Test
    public void getProgramById() throws Exception {
        Program p1 = new Program("user1");
        String p1Id = p1.getId();

        Program p2 = new Program("user1");
        String p2Id = p2.getId();

        Program p3 = new Program("user1");
        String p3Id = p3.getId();

        programMap.put(p1Id, p1);
        programMap.put(p2Id, p2);
        programMap.put(p3Id, p3);

        Program fetchedProgram = domainController.getProgramById(p1Id);
        assertSame(p1, fetchedProgram);
    }

    @Test (expected = UnknownProgramException.class)
    public void getProgramById_unknownProgram() throws Exception {
        domainController.getProgramById(UUID.randomUUID().toString());
    }

    @Test
    public void createProgram() {
        String userId = "user1";

        assertEquals(0, programMap.size());

        String pId = domainController.createProgram(userId);
        Program program = programMap.get(pId);

        assertEquals(1, programMap.size());
        assertTrue(programMap.containsKey(pId));
        assertEquals(pId, program.getId());
        assertEquals(userId, program.getUserId());
        assertEquals(ExecutionState.SCHEDULED, program.getState());
    }

    @Test
    public void getProgramDTO() throws Exception {
        Program p1 = new Program("user1");
        String p1Id = p1.getId();
        p1.setName("program1");
        p1.setDescription("desc1");
        p1.setState(ExecutionState.SCHEDULED);
        p1.setCreateTime(Instant.ofEpochMilli(rnd.nextLong()));
        p1.setFinishTime(Instant.ofEpochMilli(rnd.nextLong()));

        Program p2 = new Program("user1");
        String p2Id = p2.getId();
        p2.setName("program2");
        p2.setDescription("desc2");
        p2.setState(ExecutionState.RUN);
        p2.setCreateTime(Instant.ofEpochMilli(rnd.nextLong()));
        p2.setFinishTime(Instant.ofEpochMilli(rnd.nextLong()));

        programMap.put(p1Id, p1);
        programMap.put(p2Id, p2);

        ProgramDTO fetchedProgramDTO1 = domainController.getProgram(p1Id);
        assertEquals(p1.getId(), fetchedProgramDTO1.getId());
        assertEquals(p1.getUserId(), fetchedProgramDTO1.getUserId());
        assertEquals(p1.getName(), fetchedProgramDTO1.getName());
        assertEquals(p1.getDescription(), fetchedProgramDTO1.getDescription());
        assertEquals(p1.getState(), fetchedProgramDTO1.getState());
        assertEquals(p1.getCreateTime(), Instant.ofEpochMilli(fetchedProgramDTO1.getCreateTime()));
        assertEquals(p1.getFinishTime(), Instant.ofEpochMilli(fetchedProgramDTO1.getFinishTime()));

        ProgramDTO fetchedProgramDTO2 = domainController.getProgram(p2Id);
        assertEquals(p2.getId(), fetchedProgramDTO2.getId());
        assertEquals(p2.getUserId(), fetchedProgramDTO2.getUserId());
        assertEquals(p2.getName(), fetchedProgramDTO2.getName());
        assertEquals(p2.getDescription(), fetchedProgramDTO2.getDescription());
        assertEquals(p2.getState(), fetchedProgramDTO2.getState());
        assertEquals(p2.getCreateTime(), Instant.ofEpochMilli(fetchedProgramDTO2.getCreateTime()));
        assertEquals(p2.getFinishTime(), Instant.ofEpochMilli(fetchedProgramDTO2.getFinishTime()));
    }

    @Test (expected = UnknownProgramException.class)
    public void getProgramDTO_unknownProgram() throws Exception {
        domainController.getProgram(UUID.randomUUID().toString());
    }

    @Test
    public void getProgramOfJob() throws Exception {
    	Program program = new Program("user1");

        Job job11 = new Job(program);
        String j11Id = job11.getId();

        Job job21 = new Job(program);
        String j21Id = job21.getId();

        Job job22 = new Job(program);
        String j22Id = job22.getId();

        Program p1 = new Program("user1");
        String p1Id = p1.getId();
        p1.addJob(job11);

        Program p2 = new Program("user1");
        String p2Id = p2.getId();
        p2.addJob(job21);
        p2.addJob(job22);

        programMap.put(p1Id, p1);
        programMap.put(p2Id, p2);

        String fetchedProgramId = domainController.getProgramOfJob(j21Id);
        assertEquals(p2Id, fetchedProgramId);
    }

    @Test (expected = UnknownJobException.class)
    public void getProgramOfJob_unknownJob() throws Exception {
        Program p1 = new Program("user1");
        String p1Id = p1.getId();

        programMap.put(p1Id, p1);

        domainController.getProgramOfJob(UUID.randomUUID().toString());
    }

    @Test
    public void deleteProgram() throws Exception {
        Program program1 = new Program("user1");
        String p1Id = program1.getId();

        Job job1 = new Job(program1);
        String j1Id = job1.getId();
        program1.addJob(job1);

        String t11Id = UUID.randomUUID().toString();
        TaskDTO task11 = new TaskDTO();
        task11.setId(t11Id);
        job1.addTask(task11);

        String t12Id = UUID.randomUUID().toString();
        TaskDTO task12 = new TaskDTO();
        task12.setId(t12Id);
        job1.addTask(task12);

        Program program2 = new Program("user1");
        String p2Id = program2.getId();

        Job job2 = new Job(program2);
        String j2Id = job2.getId();
        program2.addJob(job2);

        String t21Id = UUID.randomUUID().toString();
        TaskDTO task21 = new TaskDTO();
        task21.setId(t21Id);
        job2.addTask(task21);

        String t22Id = UUID.randomUUID().toString();
        TaskDTO task22 = new TaskDTO();
        task22.setId(t22Id);
        job2.addTask(task22);

        programMap.put(p1Id, program1);
        programMap.put(p2Id, program2);
        taskCache.cache(t11Id, task11);
        taskCache.cache(t12Id, task12);
        taskCache.cache(t21Id, task21);
        taskCache.cache(t22Id, task22);

        assertEquals(2, programMap.size());
        assertTrue(programMap.containsKey(p1Id));
        assertTrue(programMap.containsKey(p2Id));
        assertTrue(taskCache.exists(t11Id));
        assertTrue(taskCache.exists(t12Id));
        assertTrue(taskCache.exists(t21Id));
        assertTrue(taskCache.exists(t22Id));

        domainController.deleteProgram(p1Id);

        assertEquals(1, programMap.size());
        assertFalse(programMap.containsKey(p1Id));
        assertTrue(programMap.containsKey(p2Id));
        assertFalse(taskCache.exists(t11Id));
        assertFalse(taskCache.exists(t12Id));
        assertTrue(taskCache.exists(t21Id));
        assertTrue(taskCache.exists(t22Id));
    }

    @Test (expected = UnknownProgramException.class)
    public void deleteProgram_unknownProgram() throws Exception {
        domainController.deleteProgram(UUID.randomUUID().toString());
    }

    @Test
    public void abortProgram() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();
        program.setState(ExecutionState.SCHEDULED);

        Job job = new Job(program);
        String jId = job.getId();
        job.setState(ExecutionState.SCHEDULED);
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setState(ExecutionState.SCHEDULED);
        task.setCreateTime(System.currentTimeMillis());
        job.addTask(task);

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        assertEquals(1, job.getScheduledTasks().size());
        assertEquals(0, job.getFailedTasks().size());

        domainController.abortProgram(pId);

        TaskDTO fetchedTask = domainController.getTask(pId, jId, tId);
        assertEquals(ExecutionState.FAILED, fetchedTask.getState());
        assertEquals(System.currentTimeMillis(), fetchedTask.getFinishTime(), 100);

        assertEquals(ExecutionState.FAILED, job.getState());
        assertEquals(0, job.getScheduledTasks().size());
        assertEquals(1, job.getFailedTasks().size());

        assertEquals(ExecutionState.FAILED, program.getState());
    }

    @Test
    public void abortProgram_programFinished() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();
        program.setState(ExecutionState.SUCCESS);

        Job job = new Job(program);
        String jId = job.getId();
        job.setState(ExecutionState.SUCCESS);
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setJobId(jId);
        task.setProgramId(pId);
        task.setState(ExecutionState.SCHEDULED);
        job.addTask(task);

        task.setState(ExecutionState.SUCCESS);
        job.notifyTaskChangedState(tId, task.getState());

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        assertEquals(1, job.getSuccessfulTasks().size());
        assertTrue(job.getSuccessfulTasks().contains(tId));

        domainController.abortProgram(pId);

        TaskDTO fetchedTask = taskCache.fetch(tId);
        assertEquals(ExecutionState.SUCCESS, fetchedTask.getState());

        assertEquals(ExecutionState.SUCCESS, job.getState());
        assertEquals(1, job.getSuccessfulTasks().size());
        assertTrue(job.getSuccessfulTasks().contains(tId));

        assertEquals(ExecutionState.SUCCESS, program.getState());
    }

    @Test (expected = UnknownProgramException.class)
    public void abortProgram_unknownProgram() throws Exception {
        domainController.abortProgram(UUID.randomUUID().toString());
    }

    @Test
    public void updateProgramName() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        String programName = "name";
        assertEquals(pId, program.getName());

        domainController.updateProgramName(pId, programName);

        assertEquals(programName, program.getName());
    }

    @Test (expected = UnknownProgramException.class)
    public void updateProgramName_unknownProgram() throws Exception {
        domainController.updateProgramName(
                UUID.randomUUID().toString(),
                "name"
        );
    }

    @Test
    public void updateProgramDescription() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        String programDescription = "description";
        assertEquals("", program.getDescription());

        domainController.updateProgramDescription(pId, programDescription);

        assertEquals(programDescription, program.getDescription());
    }

    @Test (expected = UnknownProgramException.class)
    public void updateProgramDescription_unknownProgram() throws Exception {
        domainController.updateProgramDescription(
                UUID.randomUUID().toString(),
                "description"
        );
    }

    @Test
    public void markProgramAsFinished() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        assertFalse(program.isFinished());

        domainController.markProgramAsFinished(pId);

        assertTrue(program.isFinished());
    }

    @Test (expected = UnknownProgramException.class)
    public void markProgramAsFinished_unknownProgram() throws Exception {
        domainController.markProgramAsFinished(
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void getAllSharedResources() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        ResourceDTO resource1 = new ResourceDTO();
        resource1.setId(UUID.randomUUID().toString());

        ResourceDTO resource2 = new ResourceDTO();
        resource2.setId(UUID.randomUUID().toString());

        program.addSharedResource(resource1);
        program.addSharedResource(resource2);

        programMap.put(pId, program);

        List<String> sharedResources = domainController.getAllSharedResources(pId);
        assertEquals(2, sharedResources.size());
        assertTrue(sharedResources.contains(resource1.getId()));
        assertTrue(sharedResources.contains(resource2.getId()));
    }

    @Test (expected = UnknownProgramException.class)
    public void getAllSharedResources_unknownProgram() throws Exception {
        domainController.getAllSharedResources(UUID.randomUUID().toString());
    }

    @Test
    public void createSharedResource() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        String dataTypeId = UUID.randomUUID().toString();
        ByteBuffer data = ByteBuffer.wrap(UUID.randomUUID().toString().getBytes());

        assertEquals(0, program.getSharedResources().size());

        ResourceDTO createdResource = domainController.createSharedResource(pId, dataTypeId, data);

        assertEquals(1, program.getSharedResources().size());
        assertSame(createdResource, program.getSharedResourceById(createdResource.getId()));
        assertEquals(dataTypeId, createdResource.getDataTypeId());
        assertNotNull(createdResource.getData());
    }

    @Test (expected = UnknownProgramException.class)
    public void createSharedResource_unknownProgram() throws Exception {
        domainController.createSharedResource(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null
        );
    }

    @Test
    public void getSharedResource() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        ResourceDTO resource = new ResourceDTO();
        resource.setId(UUID.randomUUID().toString());
        program.addSharedResource(resource);

        programMap.put(pId, program);

        ResourceDTO fetchedResource = domainController.getSharedResource(pId, resource.getId());
        assertSame(resource, fetchedResource);
    }

    @Test (expected = UnknownProgramException.class)
    public void getSharedResource_unknownProgram() throws Exception {
        domainController.getSharedResource(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test (expected = UnknownResourceException.class)
    public void getSharedResource_unknownResource() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.getSharedResource(pId, UUID.randomUUID().toString());
    }

    @Test
    public void deleteSharedResource_resourceExisting() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        ResourceDTO resource = new ResourceDTO();
        resource.setId(UUID.randomUUID().toString());
        program.addSharedResource(resource);

        programMap.put(pId, program);

        assertEquals(1, program.getSharedResources().size());

        domainController.deleteSharedResource(pId, resource.getId());
        assertEquals(0, program.getSharedResources().size());
    }

    @Test
    public void deleteSharedResource_resourceNotExisting() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        ResourceDTO resource = new ResourceDTO();
        resource.setId(UUID.randomUUID().toString());
        program.addSharedResource(resource);

        programMap.put(pId, program);

        assertEquals(1, program.getSharedResources().size());

        domainController.deleteSharedResource(pId, UUID.randomUUID().toString());
        assertEquals(1, program.getSharedResources().size());
    }

    @Test (expected = UnknownProgramException.class)
    public void deleteSharedResource_unknownProgram() throws Exception {
        domainController.deleteSharedResource(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test
    public void createJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();
        program.setState(ExecutionState.SCHEDULED);

        programMap.put(pId, program);

        assertEquals(0, program.getJobs().size());

        String jId = domainController.createJob(pId);
        Job job = program.getJobById(jId);
        assertEquals(1, program.getJobs().size());
        assertEquals(ExecutionState.SCHEDULED, job.getState());
    }

    @Test (expected = UnknownProgramException.class)
    public void createJob_unknownProgram() throws Exception {
        domainController.createJob(UUID.randomUUID().toString());
    }

    @Test (expected = ExecLogicException.class)
    public void createJob_programSuccessful() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();
        program.setState(ExecutionState.SUCCESS);

        programMap.put(pId, program);

        domainController.createJob(pId);
    }

    @Test (expected = ExecLogicException.class)
    public void createJob_programFailed() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();
        program.setState(ExecutionState.FAILED);

        programMap.put(pId, program);

        domainController.createJob(pId);
    }

    @Test
    public void getAllJobs() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job1 = new Job(program);
        String j1Id = job1.getId();
        program.addJob(job1);

        Job job2 = new Job(program);
        String j2Id = job2.getId();
        program.addJob(job2);

        Job job3 = new Job(program);
        String j3Id = job3.getId();
        program.addJob(job3);

        programMap.put(pId, program);

        List<String> jobIds = domainController.getAllJobs(pId);
        assertEquals(3, jobIds.size());
        assertTrue(jobIds.contains(j1Id));
        assertTrue(jobIds.contains(j2Id));
        assertTrue(jobIds.contains(j3Id));
    }

    @Test (expected = UnknownProgramException.class)
    public void getAllJobs_unknownProgram() throws Exception {
        domainController.getAllJobs(UUID.randomUUID().toString());
    }

    @Test
    public void getJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        job.setState(ExecutionState.SUCCESS);
        program.addJob(job);

        programMap.put(pId, program);

        JobDTO fetchedJob = domainController.getJob(pId, jId);
        assertEquals(jId, fetchedJob.getId());
        assertEquals(job.getCreateTime().toEpochMilli(), fetchedJob.getCreateTime());
        assertEquals(job.getState(), fetchedJob.getState());
    }

    @Test (expected = UnknownProgramException.class)
    public void getJob_unknownProgram() throws Exception {
        domainController.getJob(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }

    @Test (expected = UnknownJobException.class)
    public void getJob_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.getJob(
                pId,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void deleteJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job1 = new Job(program);
        String j1Id = job1.getId();
        program.addJob(job1);

        Job job2 = new Job(program);
        String j2Id = job2.getId();
        program.addJob(job2);

        String t11Id = UUID.randomUUID().toString();
        TaskDTO task11 = new TaskDTO();
        task11.setId(t11Id);
        job1.addTask(task11);

        String t12Id = UUID.randomUUID().toString();
        TaskDTO task12 = new TaskDTO();
        task12.setId(t12Id);
        job1.addTask(task12);

        String t21Id = UUID.randomUUID().toString();
        TaskDTO task21 = new TaskDTO();
        task21.setId(t21Id);
        job2.addTask(task21);

        String t22Id = UUID.randomUUID().toString();
        TaskDTO task22 = new TaskDTO();
        task22.setId(t22Id);
        job2.addTask(task22);

        programMap.put(pId, program);
        taskCache.cache(t11Id, task11);
        taskCache.cache(t12Id, task12);
        taskCache.cache(t21Id, task21);
        taskCache.cache(t22Id, task22);

        assertEquals(2, program.getJobs().size());
        assertTrue(taskCache.exists(t11Id));
        assertTrue(taskCache.exists(t12Id));
        assertTrue(taskCache.exists(t21Id));
        assertTrue(taskCache.exists(t22Id));

        domainController.deleteJob(pId, j1Id);

        assertEquals(1, program.getJobs().size());
        assertSame(job2, program.getJobById(j2Id));
        assertFalse(taskCache.exists(t11Id));
        assertFalse(taskCache.exists(t12Id));
        assertTrue(taskCache.exists(t21Id));
        assertTrue(taskCache.exists(t22Id));
    }

    @Test (expected = UnknownProgramException.class)
    public void deleteJob_unknownProgram() throws Exception {
        domainController.deleteJob(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }

    @Test (expected = UnknownJobException.class)
    public void deleteJob_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.deleteJob(
                pId,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void abortJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        job.setState(ExecutionState.SCHEDULED);
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setState(ExecutionState.SCHEDULED);
        job.addTask(task);

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        assertEquals(1, job.getScheduledTasks().size());
        assertEquals(0, job.getFailedTasks().size());

        domainController.abortJob(pId, jId);

        TaskDTO fetchedTask = domainController.getTask(pId, jId, tId);
        assertEquals(ExecutionState.FAILED, fetchedTask.getState());
        assertEquals(System.currentTimeMillis(), fetchedTask.getFinishTime(), 100);

        assertEquals(ExecutionState.FAILED, job.getState());
        assertEquals(0, job.getNumberOfScheduledTasks());
        assertEquals(1, job.getNumberOfFailedTasks());
    }

    @Test
    public void abortJob_jobFinished() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        job.setState(ExecutionState.SUCCESS);
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setJobId(jId);
        task.setProgramId(pId);
        task.setState(ExecutionState.SCHEDULED);
        job.addTask(task);

        task.setState(ExecutionState.SUCCESS);
        job.notifyTaskChangedState(tId, task.getState());

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        assertEquals(1, job.getSuccessfulTasks().size());
        assertTrue(job.getSuccessfulTasks().contains(tId));

        domainController.abortJob(pId, jId);

        TaskDTO fetchedTask = taskCache.fetch(tId);
        assertEquals(ExecutionState.SUCCESS, fetchedTask.getState());

        assertEquals(ExecutionState.SUCCESS, job.getState());
        assertEquals(1, job.getSuccessfulTasks().size());
        assertTrue(job.getSuccessfulTasks().contains(tId));
    }

    @Test (expected = UnknownProgramException.class)
    public void abortJob_unknownProgram() throws Exception {
        domainController.abortJob(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }

    @Test (expected = UnknownJobException.class)
    public void abortJob_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.abortJob(
                pId,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void setReducedResultsOfJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        ResourceDTO resource1 = new ResourceDTO();
        ResourceDTO resource2 = new ResourceDTO();
        List<ResourceDTO> reducedResults = Arrays.asList(resource1, resource2);

        assertNull(job.getReducedResults());

        domainController.setReducedResultsOfJob(pId, jId, reducedResults);

        assertEquals(reducedResults, job.getReducedResults());
    }

    @Test (expected = UnknownProgramException.class)
    public void setReducedResultsOfJob_unknownProgram() throws Exception {
        domainController.setReducedResultsOfJob(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new LinkedList<>()
        );
    }

    @Test (expected = UnknownJobException.class)
    public void setReducedResultsOfJob_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.setReducedResultsOfJob(
                pId,
                UUID.randomUUID().toString(),
                new LinkedList<>()
        );
    }

    @Test
    public void getAndAttachedMapRoutine() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        RoutineDTO mapRoutine = new RoutineDTO();
        mapRoutine.setId(UUID.randomUUID().toString());
        mapRoutine.setType(RoutineType.MAP);

        assertFalse(job.hasMapRoutine());

        domainController.attachMapRoutine(pId, jId, mapRoutine);

        assertTrue(job.hasMapRoutine());
        assertEquals(mapRoutine.getId(), domainController.getAttachedMapRoutine(pId, jId));
    }

    @Test
    public void getAttachedMapRoutine_hasNoMapRoutine() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        assertNull(domainController.getAttachedMapRoutine(pId, jId));
    }

    @Test (expected = ExecLogicException.class)
    public void attachMapRoutine_wrongRoutineType() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        RoutineDTO objectiveRoutine = new RoutineDTO();
        objectiveRoutine.setType(RoutineType.OBJECTIVE);

        domainController.attachMapRoutine(pId, jId, objectiveRoutine);
    }

    @Test (expected = UnknownProgramException.class)
    public void attachMapRoutine_unknownProgram() throws Exception {
        RoutineDTO routine = new RoutineDTO();

        domainController.attachMapRoutine(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                routine
        );
    }

    @Test (expected = UnknownJobException.class)
    public void attachMapRoutine_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        RoutineDTO routine = new RoutineDTO();

        programMap.put(pId, program);

        domainController.attachMapRoutine(
                pId,
                UUID.randomUUID().toString(),
                routine
        );
    }

    @Test
    public void getAndAttachReduceRoutine() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        RoutineDTO reduceRoutine = new RoutineDTO();
        reduceRoutine.setId(UUID.randomUUID().toString());
        reduceRoutine.setType(RoutineType.REDUCE);

        assertFalse(job.hasReduceRoutine());

        domainController.attachReduceRoutine(pId, jId, reduceRoutine);

        assertTrue(job.hasReduceRoutine());
        assertEquals(reduceRoutine.getId(), domainController.getAttachedReduceRoutine(pId, jId));
    }

    @Test
    public void getAttachedReduceRoutine_hasNoReduceRoutine() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        assertNull(domainController.getAttachedReduceRoutine(pId, jId));
    }

    @Test (expected = ExecLogicException.class)
    public void attachReduceRoutine_wrongRoutineType() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        RoutineDTO objectiveRoutine = new RoutineDTO();
        objectiveRoutine.setId(UUID.randomUUID().toString());
        objectiveRoutine.setType(RoutineType.OBJECTIVE);

        domainController.attachReduceRoutine(pId, jId, objectiveRoutine);
    }

    @Test (expected = UnknownProgramException.class)
    public void attachReduceRoutine_unknownProgram() throws Exception {
        RoutineDTO routine = new RoutineDTO();

        domainController.attachReduceRoutine(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                routine
        );
    }

    @Test (expected = UnknownJobException.class)
    public void attachReduceRoutine_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        RoutineDTO routine = new RoutineDTO();

        programMap.put(pId, program);

        domainController.attachReduceRoutine(
                pId,
                UUID.randomUUID().toString(),
                routine
        );
    }

    @Test
    public void markJobAsComplete() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        assertFalse(job.isComplete());

        domainController.markJobAsComplete(pId, jId);

        assertTrue(job.isComplete());
    }

    @Test (expected = UnknownProgramException.class)
    public void markJobAsComplete_unknownProgram() throws Exception {
        domainController.markJobAsComplete(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }

    @Test (expected = UnknownJobException.class)
    public void markJobAsComplete_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.markJobAsComplete(
                pId,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void hasJobReduceRoutine_hasReduceRoutine() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        RoutineDTO reduceRoutine = new RoutineDTO();
        reduceRoutine.setType(RoutineType.REDUCE);

        Job job = new Job(program);
        String jId = job.getId();
        job.setReduceRoutine(reduceRoutine);
        program.addJob(job);

        programMap.put(pId, program);

        assertTrue(domainController.hasJobReduceRoutine(pId, jId));
    }

    @Test
    public void hasJobReduceRoutine_hasNoReduceRoutine() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        assertFalse(domainController.hasJobReduceRoutine(pId, jId));
    }

    @Test (expected = UnknownProgramException.class)
    public void hasJobReduceRoutine_unknownProgram() throws Exception {
        domainController.hasJobReduceRoutine(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }

    @Test (expected = UnknownJobException.class)
    public void hasJobReduceRoutine_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.hasJobReduceRoutine(
                pId,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void allTasksOfJobSuccessful_allSuccessful() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setState(ExecutionState.SCHEDULED);
        job.addTask(task1);

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setState(ExecutionState.SCHEDULED);
        job.addTask(task2);

        String t3Id = UUID.randomUUID().toString();
        TaskDTO task3 = new TaskDTO();
        task3.setId(t3Id);
        task3.setState(ExecutionState.SCHEDULED);
        job.addTask(task3);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);
        taskCache.cache(t3Id, task3);

        task1.setState(ExecutionState.SUCCESS);
        task2.setState(ExecutionState.SUCCESS);
        task3.setState(ExecutionState.SUCCESS);
        job.notifyTaskChangedState(t1Id, task1.getState());
        job.notifyTaskChangedState(t2Id, task2.getState());
        job.notifyTaskChangedState(t3Id, task3.getState());
        job.markAsComplete();

        assertTrue(domainController.allTasksOfJobSuccessful(pId, jId));
    }

    @Test
    public void allTasksOfJobSuccessful_jobNotCompleted() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setState(ExecutionState.SCHEDULED);
        job.addTask(task1);

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setState(ExecutionState.SCHEDULED);
        job.addTask(task2);

        String t3Id = UUID.randomUUID().toString();
        TaskDTO task3 = new TaskDTO();
        task3.setId(t3Id);
        task3.setState(ExecutionState.SCHEDULED);
        job.addTask(task3);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);
        taskCache.cache(t3Id, task3);

        task1.setState(ExecutionState.SUCCESS);
        task2.setState(ExecutionState.SUCCESS);
        task3.setState(ExecutionState.SUCCESS);
        job.notifyTaskChangedState(t1Id, task1.getState());
        job.notifyTaskChangedState(t2Id, task2.getState());
        job.notifyTaskChangedState(t3Id, task3.getState());

        assertFalse(domainController.allTasksOfJobSuccessful(pId, jId));
    }

    @Test
    public void allTasksOfJobSuccessful_notAllCompleted() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setState(ExecutionState.SCHEDULED);
        job.addTask(task1);

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setState(ExecutionState.SCHEDULED);
        job.addTask(task2);

        String t3Id = UUID.randomUUID().toString();
        TaskDTO task3 = new TaskDTO();
        task3.setId(t3Id);
        task3.setState(ExecutionState.SCHEDULED);
        job.addTask(task3);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);
        taskCache.cache(t3Id, task3);

        task1.setState(ExecutionState.SUCCESS);
        task2.setState(ExecutionState.SUCCESS);
        job.notifyTaskChangedState(t1Id, task1.getState());
        job.notifyTaskChangedState(t2Id, task2.getState());

        assertFalse(domainController.allTasksOfJobSuccessful(pId, jId));
    }

    @Test
    public void allTasksOfJobSuccessful_someFailed() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setState(ExecutionState.SCHEDULED);
        job.addTask(task1);

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setState(ExecutionState.SCHEDULED);
        job.addTask(task2);

        String t3Id = UUID.randomUUID().toString();
        TaskDTO task3 = new TaskDTO();
        task3.setId(t3Id);
        task3.setState(ExecutionState.SCHEDULED);
        job.addTask(task3);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);
        taskCache.cache(t3Id, task3);

        task1.setState(ExecutionState.SUCCESS);
        task2.setState(ExecutionState.SUCCESS);
        task3.setState(ExecutionState.FAILED);
        job.notifyTaskChangedState(t1Id, task1.getState());
        job.notifyTaskChangedState(t2Id, task2.getState());
        job.notifyTaskChangedState(t3Id, task3.getState());

        assertFalse(domainController.allTasksOfJobSuccessful(pId, jId));
    }

    @Test (expected = UnknownProgramException.class)
    public void allTasksOfJobSuccessful_unknownProgram() throws Exception {
        domainController.allTasksOfJobSuccessful(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }

    @Test (expected = UnknownJobException.class)
    public void allTasksOfJobSuccessful_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.allTasksOfJobSuccessful(
                pId,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void getRunningTasksOfJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setState(ExecutionState.SCHEDULED);
        job.addTask(task1);

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setState(ExecutionState.SCHEDULED);
        job.addTask(task2);

        String t3Id = UUID.randomUUID().toString();
        TaskDTO task3 = new TaskDTO();
        task3.setId(t3Id);
        task3.setState(ExecutionState.SCHEDULED);
        job.addTask(task3);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);
        taskCache.cache(t3Id, task3);

        task1.setState(ExecutionState.RUN);
        task2.setState(ExecutionState.RUN);
        job.notifyTaskChangedState(t1Id, task1.getState());
        job.notifyTaskChangedState(t2Id, task2.getState());

        Collection<String> runningTasks = domainController.getRunningTasksOfJob(pId, jId);
        assertEquals(2, runningTasks.size());
        assertTrue(runningTasks.contains(t1Id));
        assertTrue(runningTasks.contains(t2Id));
        assertFalse(runningTasks.contains(t3Id));
    }

    @Test (expected = UnknownProgramException.class)
    public void getRunningTasksOfJob_unknownProgram() throws Exception {
        domainController.getRunningTasksOfJob(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
    }

    @Test (expected = UnknownJobException.class)
    public void getRunningTasksOfJob_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.getRunningTasksOfJob(
                pId,
                UUID.randomUUID().toString()
        );
    }

    @Test
    public void getResourcesWithSpecificKeys() throws Exception {
        String resourceKey1 = "key1";
        String resourceKey2 = "key2";
        String resourceKey3 = "key3";
        String resourceKey4 = "key4";

        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        ResourceDTO resource1 = new ResourceDTO();
        resource1.setKey(resourceKey1);

        ResourceDTO resource2 = new ResourceDTO();
        resource2.setKey(resourceKey2);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setState(ExecutionState.SCHEDULED);
        task1.addToOutParameters(resource1);
        task1.addToOutParameters(resource2);
        job.addTask(task1);

        ResourceDTO resource3 = new ResourceDTO();
        resource3.setKey(resourceKey3);

        ResourceDTO resource4 = new ResourceDTO();
        resource4.setKey(UUID.randomUUID().toString());

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setState(ExecutionState.SCHEDULED);
        task2.addToOutParameters(resource3);
        task2.addToOutParameters(resource4);
        job.addTask(task2);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);

        List<String> resourceKeys = new LinkedList<>();
        resourceKeys.add(resourceKey1);
        resourceKeys.add(resourceKey2);
        resourceKeys.add(resourceKey3);
        resourceKeys.add(resourceKey4);

        List<ResourceDTO> fetchedResources = domainController.getResourcesWithSpecificKeys(jId, resourceKeys);
        List<String> fetchedResourceKeys = fetchedResources.stream().map(ResourceDTO::getKey).collect(Collectors.toList());
        assertEquals(3, fetchedResources.size());
        assertTrue(fetchedResourceKeys.contains(resourceKey1));
        assertTrue(fetchedResourceKeys.contains(resourceKey2));
        assertTrue(fetchedResourceKeys.contains(resourceKey3));
        assertFalse(fetchedResourceKeys.contains(resourceKey4));
    }

    @Test (expected = UnknownJobException.class)
    public void getResourcesWithSpecificKeys_unknownJob() throws Exception {
        domainController.getResourcesWithSpecificKeys(
                UUID.randomUUID().toString(),
                new LinkedList<>()
        );
    }

    @Test
    public void createEmptyTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();

        program.addJob(job);

        programMap.put(pId, program);

        String taskId = domainController.createEmptyTask(pId, jId);
        TaskDTO createdTask = taskCache.fetch(taskId);

        assertNotNull(taskId);
        assertNotNull(createdTask);
        assertEquals(taskId, createdTask.getId());
        assertEquals(jId, createdTask.getJobId());
        assertEquals(pId, createdTask.getProgramId());
        assertEquals(System.currentTimeMillis(), createdTask.getCreateTime(), 100);
        assertEquals(ExecutionState.SCHEDULED, createdTask.getState());
        assertEquals(1, job.getScheduledTasks().size());
        assertTrue(job.getScheduledTasks().contains(taskId));
    }

    @Test (expected = ExecLogicException.class)
    public void createEmptyTask_jobCompleted() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();

        job.setComplete(true);
        program.addJob(job);

        programMap.put(pId, program);

        domainController.createEmptyTask(pId, jId);
    }

    @Test (expected = UnknownProgramException.class)
    public void createEmptyTask_unknownProgram() throws Exception {
        domainController.createEmptyTask(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test (expected = UnknownJobException.class)
    public void createEmptyTask_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.createEmptyTask(pId, UUID.randomUUID().toString());
    }

    @Test
    public void configureTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        RoutineDTO mapRoutine = new RoutineDTO();
        mapRoutine.setId(UUID.randomUUID().toString());
        mapRoutine.setType(RoutineType.MAP);

        Job job = new Job(program);
        String jId = job.getId();
        job.setMapRoutine(mapRoutine);
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setJobId(jId);
        task.setProgramId(pId);
        task.setState(ExecutionState.SCHEDULED);
        job.addTask(task);

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        DataTypeDTO dataType = new DataTypeDTO();
        dataType.setId(UUID.randomUUID().toString());

        FormalParameterDTO parameter = new FormalParameterDTO();
        parameter.setId(UUID.randomUUID().toString());
        parameter.setName("parameter1");
        parameter.setDataType(dataType);

        ResourceDTO resource = new ResourceDTO();
        resource.setId(UUID.randomUUID().toString());
        resource.setData(new byte[2]);
        resource.setDataTypeId(dataType.getId());

        RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
        routineInstance.putToInParameters(parameter.getName(), resource);

        RoutineDTO objectiveRoutine = new RoutineDTO();
        objectiveRoutine.setId(UUID.randomUUID().toString());
        objectiveRoutine.addToInParameters(parameter);
        objectiveRoutine.setType(RoutineType.OBJECTIVE);

        TaskDTO fetchedTask = domainController.configureTask(pId, jId, tId, routineInstance, objectiveRoutine);
        assertEquals(objectiveRoutine.getId(), fetchedTask.getObjectiveRoutineId());
        assertEquals(mapRoutine.getId(), fetchedTask.getMapRoutineId());
        assertEquals(routineInstance.getInParameters(), task.getInParameters());
    }

    @Test (expected = UnknownProgramException.class)
    public void configureTask_unknownProgram() throws Exception {
        RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
        RoutineDTO routine = new RoutineDTO();
        routine.setInParameters(new LinkedList<>());
        domainController.configureTask(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                routineInstance,
                routine
        );
    }

    @Test (expected = UnknownJobException.class)
    public void configureTask_unknownJob() throws Exception {
        RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
        RoutineDTO routine = new RoutineDTO();
        routine.setInParameters(new LinkedList<>());

        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.configureTask(
                pId,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                routineInstance,
                routine
        );
    }

    @Test (expected = UnknownTaskException.class)
    public void configureTask_unknownTask() throws Exception {
        RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
        RoutineDTO routine = new RoutineDTO();
        routine.setInParameters(new LinkedList<>());

        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        domainController.configureTask(
                pId,
                jId,
                UUID.randomUUID().toString(),
                routineInstance,
                routine
        );
    }

    @Test (expected = ExecLogicException.class)
    public void configureTask_missingParameters() throws Exception {
        List<String> missingParameters = new LinkedList<>();
        missingParameters.add(UUID.randomUUID().toString());

        RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
        routineInstance.setMissingParameters(missingParameters);

        RoutineDTO routine = new RoutineDTO();

        domainController.configureTask(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                routineInstance,
                routine
        );
    }

    @Test (expected = ExecLogicException.class)
    public void configureTask_failedParameterCheck() throws Exception {
        FormalParameterDTO parameter = new FormalParameterDTO();
        parameter.setName("parameter1");

        RoutineDTO objectiveRoutine = new RoutineDTO();
        objectiveRoutine.addToInParameters(parameter);

        RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
        routineInstance.setInParameters(new HashMap<>());

        domainController.configureTask(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                routineInstance,
                objectiveRoutine
        );
    }

    @Test
    public void getAllTasks() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setJobId(jId);
        task1.setProgramId(pId);
        task1.setState(ExecutionState.SCHEDULED);
        job.addTask(task1);

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setJobId(jId);
        task2.setProgramId(pId);
        task2.setState(ExecutionState.SCHEDULED);
        job.addTask(task2);

        String t3Id = UUID.randomUUID().toString();
        TaskDTO task3 = new TaskDTO();
        task3.setId(t3Id);
        task3.setJobId(jId);
        task3.setProgramId(pId);
        task3.setState(ExecutionState.SCHEDULED);
        job.addTask(task3);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);
        taskCache.cache(t3Id, task3);

        List<String> taskIds = domainController.getAllTasks(pId, jId, SortingCriterion.NO_SORTING);
        assertTrue(taskIds.contains(t1Id));
        assertTrue(taskIds.contains(t2Id));
        assertTrue(taskIds.contains(t3Id));
    }

    @Test (expected = UnknownProgramException.class)
    public void getAllTasks_unknownProgram() throws Exception {
        domainController.getAllTasks(UUID.randomUUID().toString(), UUID.randomUUID().toString(), SortingCriterion.NO_SORTING);
    }

    @Test (expected = UnknownJobException.class)
    public void getAllTasks_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.getAllTasks(pId, UUID.randomUUID().toString(), SortingCriterion.NO_SORTING);
    }

    @Test (expected = ExecLogicException.class)
    public void getAllTasks_unknownTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        TaskDTO task = new TaskDTO();
        String tId = UUID.randomUUID().toString();
        task.setId(tId);
        job.addTask(task);

        programMap.put(pId, program);

        domainController.getAllTasks(pId, jId, SortingCriterion.CREATION_DATE_FROM_OLDEST);
    }

    @Test
    public void getAllTasksWithState() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setJobId(jId);
        task1.setProgramId(pId);
        task1.setState(ExecutionState.SCHEDULED);
        job.addTask(task1);

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setJobId(jId);
        task2.setProgramId(pId);
        task2.setState(ExecutionState.SCHEDULED);
        job.addTask(task2);

        String t3Id = UUID.randomUUID().toString();
        TaskDTO task3 = new TaskDTO();
        task3.setId(t3Id);
        task3.setJobId(jId);
        task3.setProgramId(pId);
        task3.setState(ExecutionState.SCHEDULED);
        job.addTask(task3);

        String t4Id = UUID.randomUUID().toString();
        TaskDTO task4 = new TaskDTO();
        task4.setId(t4Id);
        task4.setJobId(jId);
        task4.setProgramId(pId);
        task4.setState(ExecutionState.SCHEDULED);
        job.addTask(task4);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);
        taskCache.cache(t3Id, task3);
        taskCache.cache(t4Id, task4);

        List<String> scheduledTasks = domainController.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING);
        assertEquals(4, scheduledTasks.size());
        assertTrue(scheduledTasks.contains(t1Id));
        assertTrue(scheduledTasks.contains(t2Id));
        assertTrue(scheduledTasks.contains(t3Id));
        assertTrue(scheduledTasks.contains(t4Id));

        task2.setState(ExecutionState.RUN);
        task3.setState(ExecutionState.RUN);
        task4.setState(ExecutionState.RUN);
        job.notifyTaskChangedState(t2Id, task2.getState());
        job.notifyTaskChangedState(t3Id, task3.getState());
        job.notifyTaskChangedState(t4Id, task4.getState());
        taskCache.cache(t2Id, task2);
        taskCache.cache(t3Id, task3);
        taskCache.cache(t4Id, task4);

        scheduledTasks = domainController.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING);
        List<String> runningTasks = domainController.getAllTasksWithState(pId, jId, ExecutionState.RUN, SortingCriterion.NO_SORTING);
        assertEquals(1, scheduledTasks.size());
        assertEquals(3, runningTasks.size());
        assertTrue(scheduledTasks.contains(t1Id));
        assertTrue(runningTasks.contains(t2Id));
        assertTrue(runningTasks.contains(t3Id));
        assertTrue(runningTasks.contains(t4Id));

        task3.setState(ExecutionState.SUCCESS);
        task4.setState(ExecutionState.FAILED);
        job.notifyTaskChangedState(t3Id, task3.getState());
        job.notifyTaskChangedState(t4Id, task4.getState());
        taskCache.cache(t3Id, task3);
        taskCache.cache(t4Id, task4);

        scheduledTasks = domainController.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING);
        runningTasks = domainController.getAllTasksWithState(pId, jId, ExecutionState.RUN, SortingCriterion.NO_SORTING);
        List<String> successfulTasks = domainController.getAllTasksWithState(pId, jId, ExecutionState.SUCCESS, SortingCriterion.NO_SORTING);
        List<String> failedTasks = domainController.getAllTasksWithState(pId, jId, ExecutionState.FAILED, SortingCriterion.NO_SORTING);
        assertEquals(1, scheduledTasks.size());
        assertEquals(1, runningTasks.size());
        assertEquals(1, successfulTasks.size());
        assertEquals(1, failedTasks.size());
        assertTrue(scheduledTasks.contains(t1Id));
        assertTrue(runningTasks.contains(t2Id));
        assertTrue(successfulTasks.contains(t3Id));
        assertTrue(failedTasks.contains(t4Id));
    }

    @Test (expected = UnknownProgramException.class)
    public void getAllTasksWithState_unknownProgram() throws Exception {
        domainController.getAllTasksWithState(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                ExecutionState.FAILED,
                SortingCriterion.NO_SORTING
        );
    }

    @Test (expected = UnknownJobException.class)
    public void getAllTasksWithState_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.getAllTasksWithState(
                pId,
                UUID.randomUUID().toString(),
                ExecutionState.SUCCESS,
                SortingCriterion.NO_SORTING
        );
    }

    @Test (expected = ExecLogicException.class)
    public void getAllTasksWithState_unknownTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        TaskDTO task = new TaskDTO();
        task.setId(UUID.randomUUID().toString());
        job.addTask(task);

        programMap.put(pId, program);

        domainController.getAllTasksWithState(
                pId,
                jId,
                ExecutionState.SCHEDULED,
                SortingCriterion.CREATION_DATE_FROM_NEWEST
        );
    }

    @Test
    public void getTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setState(ExecutionState.SUCCESS);
        task.setStartTime(10);
        task.setFinishTime(20);
        job.addTask(task);

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        TaskDTO fetchedTask = domainController.getTask(pId, jId, tId);
        assertSame(task, fetchedTask);
        assertEquals(task.getFinishTime() - task.getStartTime(), fetchedTask.getRuntime());
    }

    @Test (expected = UnknownProgramException.class)
    public void getTask_unknownProgram() throws Exception {
        domainController.getTask(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test (expected = UnknownJobException.class)
    public void getTask_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.getTask(pId, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test (expected = UnknownTaskException.class)
    public void getTask_unknownTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        domainController.getTask(pId, jId, UUID.randomUUID().toString());
    }

    @Test
    public void fetchTaskFromCache() throws Exception {
    	Program program = new Program("user1");
    	Job job = new Job(program);
        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        job.addTask(task);
        programMap.put(program.getId(), program);

        taskCache.cache(tId, task);

        TaskDTO fetchedTask = domainController.getTask(tId);

        assertSame(task, fetchedTask);
    }

    @Test (expected = UnknownTaskException.class)
    public void fetchTaskFromCache_unknownTask() throws Exception {
        domainController.getTask(UUID.randomUUID().toString());
    }

    @Test
    public void getTaskPartial() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setState(ExecutionState.SUCCESS);
        task.setInParameters(new HashMap<>());
        task.setOutParameters(new LinkedList<>());
        job.addTask(task);

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        TaskDTO fetchedTask = domainController.getTaskPartial(pId, jId, tId, true, true);
        assertNotNull(fetchedTask.getInParameters());
        assertNotNull(fetchedTask.getOutParameters());

        fetchedTask = domainController.getTaskPartial(pId, jId, tId, true, false);
        assertNotNull(fetchedTask.getInParameters());
        assertNull(fetchedTask.getOutParameters());

        fetchedTask = domainController.getTaskPartial(pId, jId, tId, false, true);
        assertNull(fetchedTask.getInParameters());
        assertNotNull(fetchedTask.getOutParameters());

        fetchedTask = domainController.getTaskPartial(pId, jId, tId, false, false);
        assertNull(fetchedTask.getInParameters());
        assertNull(fetchedTask.getOutParameters());
    }

    @Test (expected = UnknownProgramException.class)
    public void getTaskPartial_unknownProgram() throws Exception {
        domainController.getTaskPartial(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), true, true);
    }

    @Test (expected = UnknownJobException.class)
    public void getTaskPartial_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.getTaskPartial(pId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), true, true);
    }

    @Test (expected = UnknownTaskException.class)
    public void getTaskPartial_unknownTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        domainController.getTaskPartial(pId, jId, UUID.randomUUID().toString(), true, true);
    }

    @Test
    public void abortTask_scheduledTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setState(ExecutionState.SCHEDULED);
        job.addTask(task);

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        domainController.abortTask(pId, jId, tId);

        TaskDTO fetchedTask = taskCache.fetch(tId);
        assertEquals(ExecutionState.FAILED, fetchedTask.getState());
        assertEquals(System.currentTimeMillis(), fetchedTask.getFinishTime(), 100);
        assertTrue(fetchedTask.getRuntime() > 0);
        assertTrue(job.getFailedTasks().contains(tId));
    }

    @Test
    public void abortTask_successfulTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setState(ExecutionState.SCHEDULED);
        job.addTask(task);
        task.setState(ExecutionState.SUCCESS);
        job.notifyTaskChangedState(tId, ExecutionState.SUCCESS);

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        assertTrue(job.getSuccessfulTasks().contains(tId));

        domainController.abortTask(pId, jId, tId);

        TaskDTO fetchedTask = taskCache.fetch(tId);
        assertEquals(task.getState(), fetchedTask.getState());
        assertEquals(task.getRuntime(), fetchedTask.getRuntime());
        assertTrue(job.getSuccessfulTasks().contains(tId));
    }

    @Test (expected = UnknownProgramException.class)
    public void abortTask_unknownProgram() throws Exception {
        domainController.abortTask(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test (expected = UnknownJobException.class)
    public void abortTask_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.abortTask(pId, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test (expected = UnknownTaskException.class)
    public void abortTask_unknownTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        domainController.abortTask(pId, jId, UUID.randomUUID().toString());
    }

    @Test
    public void fetchAndPrepareTaskForReRun() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        String tId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();
        task.setId(tId);
        task.setState(ExecutionState.SCHEDULED);
        job.addTask(task);
        task.setState(ExecutionState.SUCCESS);
        job.notifyTaskChangedState(tId, ExecutionState.SUCCESS);

        programMap.put(pId, program);
        taskCache.cache(tId, task);

        assertTrue(job.getSuccessfulTasks().contains(tId));

        TaskDTO fetchedTask = domainController.fetchAndPrepareTaskForReRun(pId, jId, tId);
        TaskDTO cachedTask = taskCache.fetch(tId);

        assertSame(fetchedTask, cachedTask);
        assertEquals(ExecutionState.SCHEDULED, fetchedTask.getState());
        assertTrue(job.getScheduledTasks().contains(tId));
    }


    @Test (expected = UnknownProgramException.class)
    public void fetchAndPrepareTaskForReRun_unknownProgram() throws Exception {
        domainController.fetchAndPrepareTaskForReRun(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test (expected = UnknownJobException.class)
    public void fetchAndPrepareTaskForReRun_unknownJob() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.fetchAndPrepareTaskForReRun(pId, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test (expected = UnknownTaskException.class)
    public void fetchAndPrepareTaskForReRun_unknownTask() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        program.addJob(job);

        programMap.put(pId, program);

        domainController.fetchAndPrepareTaskForReRun(pId, jId, UUID.randomUUID().toString());
    }

    @Test
    public void notifyTaskSuccess() throws Exception {
        // Prepare routines
        RoutineDTO objectiveRoutine = new RoutineDTO();
        objectiveRoutine.setType(RoutineType.OBJECTIVE);
        objectiveRoutine.setId(UUID.randomUUID().toString());

        RoutineDTO mapRoutine = new RoutineDTO();
        mapRoutine.setType(RoutineType.MAP);
        mapRoutine.setId(UUID.randomUUID().toString());

        // Prepare program, job, task
        Program program = new Program("user1");
        String pId = program.getId();

        Job job = new Job(program);
        String jId = job.getId();
        job.setMapRoutine(mapRoutine);
        program.addJob(job);

        String t1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(t1Id);
        task1.setObjectiveRoutineId(objectiveRoutine.getId());
        task1.setMapRoutineId(mapRoutine.getId());
        job.addTask(task1);
        task1.setState(ExecutionState.RUN);

        String t2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(t2Id);
        task2.setObjectiveRoutineId(objectiveRoutine.getId());
        task2.setMapRoutineId(mapRoutine.getId());
        job.addTask(task2);
        task2.setState(ExecutionState.RUN);

        programMap.put(pId, program);
        taskCache.cache(t1Id, task1);
        taskCache.cache(t2Id, task2);

        task1.setState(ExecutionState.SUCCESS);
        domainController.notifyTasksChangedState(Collections.singletonList(task1.getId()), ExecutionState.SUCCESS);
        TaskDTO fetchedTask = domainController.getTask(pId, jId, t1Id);
        assertEquals(ExecutionState.SUCCESS, fetchedTask.getState());
        assertTrue(job.getSuccessfulTasks().contains(t1Id));
        assertFalse(job.getSuccessfulTasks().contains(t2Id));

        task2.setState(ExecutionState.SUCCESS);
        domainController.notifyTasksChangedState(Collections.singletonList(task2.getId()), ExecutionState.SUCCESS);
        fetchedTask = taskCache.fetch(t2Id);
        assertEquals(ExecutionState.SUCCESS, fetchedTask.getState());
        assertTrue(job.getSuccessfulTasks().contains(t1Id));
        assertTrue(job.getSuccessfulTasks().contains(t2Id));
    }

    @Test
    public void notifyTasksRun() throws Exception {
        Program program = new Program("user1");
        String programId = program.getId();

        Job job = new Job(program);
        String jobId = job.getId();
        job.setState(ExecutionState.SCHEDULED);


        String task1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(task1Id);
        task1.setJobId(jobId);
        task1.setProgramId(programId);
        task1.setState(ExecutionState.SCHEDULED);
        task1.setCreateTime(System.currentTimeMillis());

        String task2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(task2Id);
        task2.setState(ExecutionState.SCHEDULED);
        task2.setCreateTime(System.currentTimeMillis());

        job.addTask(task1);
        job.addTask(task2);
        program.addJob(job);
        programMap.put(programId, program);
        taskCache.cache(task1Id, task1);
        taskCache.cache(task2Id, task2);

        assertEquals(2, job.getScheduledTasks().size());
        assertEquals(ExecutionState.SCHEDULED, job.getState());

        domainController.notifyTasksChangedState(Arrays.asList(task1Id), ExecutionState.RUN);
        assertEquals(1, job.getScheduledTasks().size());
        assertEquals(1, job.getRunningTasks().size());
        assertEquals(ExecutionState.RUN, job.getState());
        TaskDTO fetchedTask = taskCache.fetch(task1Id);
        assertEquals(ExecutionState.RUN, fetchedTask.getState());
        assertEquals(System.currentTimeMillis(), fetchedTask.getStartTime(), 100);

        domainController.notifyTasksChangedState(Arrays.asList(task2Id), ExecutionState.RUN);
        assertEquals(0, job.getScheduledTasks().size());
        assertEquals(2, job.getRunningTasks().size());
        fetchedTask = taskCache.fetch(task2Id);
        assertEquals(ExecutionState.RUN, fetchedTask.getState());
        assertEquals(System.currentTimeMillis(), fetchedTask.getStartTime(), 100);
    }

    @Test
    public void sortTasks() throws Exception {
        String task1Id = UUID.randomUUID().toString();
        TaskDTO task1 = new TaskDTO();
        task1.setId(task1Id);
        task1.setState(ExecutionState.SUCCESS);
        task1.setCreateTime(10);
        task1.setStartTime(20);
        task1.setFinishTime(30);

        String task2Id = UUID.randomUUID().toString();
        TaskDTO task2 = new TaskDTO();
        task2.setId(task2Id);
        task2.setState(ExecutionState.SUCCESS);
        task2.setCreateTime(50);
        task2.setStartTime(70);
        task2.setFinishTime(90);

        String task3Id = UUID.randomUUID().toString();
        TaskDTO task3 = new TaskDTO();
        task3.setId(task3Id);
        task3.setState(ExecutionState.SUCCESS);
        task3.setCreateTime(5);
        task3.setStartTime(30);
        task3.setFinishTime(100);

        List<String> taskIds = Arrays.asList(task1Id, task2Id, task3Id);

        taskCache.cache(task1Id, task1);
        taskCache.cache(task2Id, task2);
        taskCache.cache(task3Id, task3);

        List<String> sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.NO_SORTING);
        assertEquals(task1Id, sortedTasks.get(0));
        assertEquals(task2Id, sortedTasks.get(1));
        assertEquals(task3Id, sortedTasks.get(2));

        sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.CREATION_DATE_FROM_NEWEST);
        assertEquals(task2Id, sortedTasks.get(0));
        assertEquals(task1Id, sortedTasks.get(1));
        assertEquals(task3Id, sortedTasks.get(2));

        sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.CREATION_DATE_FROM_OLDEST);
        assertEquals(task3Id, sortedTasks.get(0));
        assertEquals(task1Id, sortedTasks.get(1));
        assertEquals(task2Id, sortedTasks.get(2));

        sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.START_DATE_FROM_NEWEST);
        assertEquals(task2Id, sortedTasks.get(0));
        assertEquals(task3Id, sortedTasks.get(1));
        assertEquals(task1Id, sortedTasks.get(2));

        sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.START_DATE_FROM_OLDEST);
        assertEquals(task1Id, sortedTasks.get(0));
        assertEquals(task3Id, sortedTasks.get(1));
        assertEquals(task2Id, sortedTasks.get(2));

        sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.FINISH_DATE_FROM_NEWEST);
        assertEquals(task3Id, sortedTasks.get(0));
        assertEquals(task2Id, sortedTasks.get(1));
        assertEquals(task1Id, sortedTasks.get(2));

        sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.FINISH_DATE_FROM_OLDEST);
        assertEquals(task1Id, sortedTasks.get(0));
        assertEquals(task2Id, sortedTasks.get(1));
        assertEquals(task3Id, sortedTasks.get(2));

        sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.RUNTIME_FROM_LONGEST);
        assertEquals(task3Id, sortedTasks.get(0));
        assertEquals(task2Id, sortedTasks.get(1));
        assertEquals(task1Id, sortedTasks.get(2));

        sortedTasks = domainController.sortTasks(taskIds, SortingCriterion.RUNTIME_FROM_SHORTEST);
        assertEquals(task1Id, sortedTasks.get(0));
        assertEquals(task2Id, sortedTasks.get(1));
        assertEquals(task3Id, sortedTasks.get(2));
    }

    @Test (expected = UnknownTaskException.class)
    public void sortTasks_unknownTask() throws Exception {
        domainController.sortTasks(Arrays.asList(UUID.randomUUID().toString()), SortingCriterion.CREATION_DATE_FROM_NEWEST);
    }

    @Test
    public void compareTasks() {
        TaskDTO task1 = new TaskDTO();
        task1.setCreateTime(10);
        task1.setStartTime(20);
        task1.setFinishTime(30);
        task1.setRuntime(10);

        TaskDTO task2 = new TaskDTO();
        task2.setCreateTime(50);
        task2.setStartTime(70);
        task2.setFinishTime(90);
        task2.setRuntime(20);

        assertEquals(1, domainController.compareTasks(task1, task2, SortingCriterion.CREATION_DATE_FROM_NEWEST));
        assertEquals(-1, domainController.compareTasks(task1, task2, SortingCriterion.CREATION_DATE_FROM_OLDEST));
        assertEquals(1, domainController.compareTasks(task1, task2, SortingCriterion.START_DATE_FROM_NEWEST));
        assertEquals(-1, domainController.compareTasks(task1, task2, SortingCriterion.START_DATE_FROM_OLDEST));
        assertEquals(1, domainController.compareTasks(task1, task2, SortingCriterion.FINISH_DATE_FROM_NEWEST));
        assertEquals(-1, domainController.compareTasks(task1, task2, SortingCriterion.FINISH_DATE_FROM_OLDEST));
        assertEquals(1, domainController.compareTasks(task1, task2, SortingCriterion.RUNTIME_FROM_LONGEST));
        assertEquals(-1, domainController.compareTasks(task1, task2, SortingCriterion.RUNTIME_FROM_SHORTEST));
        assertEquals(-1, domainController.compareTasks(task1, task2, SortingCriterion.NO_SORTING));
    }

    @Test
    public void compareByTime() {
        assertEquals(-1, domainController.compareByTime(10, 5));
        assertEquals(0, domainController.compareByTime(10, 10));
        assertEquals(1, domainController.compareByTime(5, 10));
    }

    @Test
    public void calcRuntimeForTask() {
        TaskDTO task = new TaskDTO();

        task.setState(ExecutionState.SCHEDULED);
        assertEquals(0, domainController.calcRuntimeForTask(task));

        task.setState(ExecutionState.RUN);
        task.setStartTime(0);
        assertEquals(System.currentTimeMillis(), domainController.calcRuntimeForTask(task), 100);

        task.setState(ExecutionState.SUCCESS);
        task.setStartTime(rnd.nextLong());
        task.setFinishTime(rnd.nextLong());
        assertEquals(task.getFinishTime() - task.getStartTime(), domainController.calcRuntimeForTask(task));

        task.setState(ExecutionState.FAILED);
        assertEquals(task.getFinishTime() - task.getStartTime(), domainController.calcRuntimeForTask(task));
    }

    @Test
    public void getDataTypeIdOfResource_noSharedResource() throws Exception {
        Program program = new Program("user1");
        String pId = program.getId();
        String dataTypeId = UUID.randomUUID().toString();

        ResourceDTO resource = new ResourceDTO();
        resource.setId(UUID.randomUUID().toString());
        resource.setData(new byte[2]);
        resource.setDataTypeId(dataTypeId);

        String fetchedDataTypeId = domainController.getDataTypeIdOfResource(pId, resource);
        assertEquals(dataTypeId, fetchedDataTypeId);
    }

    @Test
    public void getDataTypeIdOfResource_sharedResource() throws Exception {
        String dataTypeId = UUID.randomUUID().toString();

        String sharedResourceId = UUID.randomUUID().toString();
        ResourceDTO sharedResource = new ResourceDTO();
        sharedResource.setId(sharedResourceId);
        sharedResource.setDataTypeId(dataTypeId);

        Program program = new Program("user1");
        String pId = program.getId();
        program.addSharedResource(sharedResource);

        programMap.put(pId, program);

        String fetchedDataTypeId = domainController.getDataTypeIdOfResource(pId, sharedResource);
        assertEquals(dataTypeId, fetchedDataTypeId);
    }

    @Test (expected = UnknownProgramException.class)
    public void getDataTypeIdOfResource_unknownProgram() throws Exception {
        String resourceId = UUID.randomUUID().toString();
        ResourceDTO resource = new ResourceDTO();
        resource.setId(resourceId);

        domainController.getDataTypeIdOfResource(UUID.randomUUID().toString(), resource);
    }

    @Test (expected = ExecLogicException.class)
    public void getDataTypeIdOfResource_unknownSharedResource() throws Exception {
        String resourceId = UUID.randomUUID().toString();
        ResourceDTO resource = new ResourceDTO();
        resource.setId(resourceId);

        Program program = new Program("user1");
        String pId = program.getId();

        programMap.put(pId, program);

        domainController.getDataTypeIdOfResource(pId, resource);
    }

    @Test
    public void checkForValidDatatype() throws Exception {
        String dataTypeId = UUID.randomUUID().toString();
        DataTypeDTO dataType = new DataTypeDTO();
        dataType.setId(dataTypeId);
        FormalParameterDTO parameter = new FormalParameterDTO();
        parameter.setDataType(dataType);

        assertTrue(domainController.checkForValidDatatype(parameter, dataTypeId));
    }

    @Test (expected = ExecLogicException.class)
    public void checkForValidDataType_failed() throws Exception {
        String dataTypeId = UUID.randomUUID().toString();
        DataTypeDTO dataType = new DataTypeDTO();
        dataType.setId(dataTypeId);
        FormalParameterDTO parameter = new FormalParameterDTO();
        parameter.setDataType(dataType);

        domainController.checkForValidDatatype(parameter, UUID.randomUUID().toString());
    }
}
