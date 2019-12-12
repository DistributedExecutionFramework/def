package at.enfilo.def.persistence.misc;

import at.enfilo.def.common.util.DEFHashHelper;
import at.enfilo.def.domain.entity.*;
import at.enfilo.def.persistence.api.*;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.dto.ExecutionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by mase on 17.08.2016.
 */
public class FillTables {

    private static final Logger LOGGER = LoggerFactory.getLogger(FillTables.class);

    private static final String USER_NAME_FORMAT = "User #%s";
    private static final String GROUP_NAME_FORMAT = "Group #%s";

    private static final int NUMBER_OF_USERS = 3;
    private static final int NUMBER_OF_GROUPS = 2;
    private static final int NUMBER_OF_PROGRAMS = 3;
    private static final int NUMBER_OF_JOBS = 2;
    private static final int NUMBER_OF_TASKS = 2;

    private FillTables() {
        // Hiding public constructor
    }

    public static void main(String[] args)
    throws PersistenceException {

        System.out.println(
            "PLEASE UNCOMMENT \"javax.persistence.schema-generation.database.action\" IN \"hibernate.cfg.xml\" BEFORE EXECUTION.\n" +
            "DO NOT FORGET TO RECOMMENT IT AFTERWARDS!"
        );

        IPersistenceFacade persistenceFacade = new PersistenceFacade();

        // GROUPS

        Group[] groupRegistry = new Group[NUMBER_OF_GROUPS];
        IGroupDAO groupDAO = persistenceFacade.getNewGroupDAO();

        for (int i = 0; i < NUMBER_OF_GROUPS; ++i) {
            String groupName = String.format(GROUP_NAME_FORMAT, i);

            Group group = new Group();
            group.setName(groupName);
            group.setDescription(String.valueOf(System.nanoTime()));

            groupDAO.save(group);
            groupRegistry[i] = group;
        }

        // USERS

        User[] userRegistry = new User[NUMBER_OF_USERS];
        IUserDAO userDAO = persistenceFacade.getNewUserDAO();

        for (int i = 0;  i < NUMBER_OF_USERS; ++i) {
            String username = String.format(USER_NAME_FORMAT, i);
            String password = String.valueOf(i);
            String salt = DEFHashHelper.generateNewSalt();

            User user = new User();
            user.setName(username);
            user.setSalt(salt);
            user.setPass(DEFHashHelper.getPasswordHash(password, salt));

            userDAO.save(user);
            userRegistry[i] = user;

            int numberOfGroupAssociations = ThreadLocalRandom.current().nextInt(1, NUMBER_OF_GROUPS + 1);
            Set<Integer> associationControl = new HashSet<>();
            Set<Group> groups = new HashSet<>();

            setAssociations(
                numberOfGroupAssociations,
                NUMBER_OF_GROUPS,
                groupRegistry,
                associationControl,
                groups::add
            );

            user.setGroups(groups);
            userDAO.saveOrUpdate(user);
        }

        // Required to avoid overriding the db.
        groupRegistry = getSynchronizedRegistry(Group[]::new, groupDAO::findAll);
        userRegistry = getSynchronizedRegistry(User[]::new, userDAO::findAll);

        // PROGRAMS

        Program[] programRegistry = new Program[NUMBER_OF_PROGRAMS];
        IProgramDAO programDAO = persistenceFacade.getNewProgramDAO();

        for (int i = 0; i < NUMBER_OF_PROGRAMS; ++i) {

            int userAssociationId = ThreadLocalRandom.current().nextInt(0, NUMBER_OF_USERS);
            User owner = userRegistry[userAssociationId];

            Program program = new Program();
            program.setOwner(owner);
            program.setState(ExecutionState.SCHEDULED);
            program.setCreateTime(Instant.now());

            programDAO.save(program);
            programRegistry[i] = program;

            int numberOfGroupAssociations = ThreadLocalRandom.current().nextInt(1, NUMBER_OF_GROUPS + 1);
            Set<Integer> associationControl = new HashSet<>();
            Set<Group> groups = new HashSet<>();

            setAssociations(
                numberOfGroupAssociations,
                NUMBER_OF_GROUPS,
                groupRegistry,
                associationControl,
                groups::add
            );

            program.setGroups(groups);
            programDAO.saveOrUpdate(program);
        }

        // Required to avoid overriding the db.
        programRegistry = getSynchronizedRegistry(Program[]::new, programDAO::findAll);

        // JOBS

        Job[] jobRegistry = new Job[NUMBER_OF_JOBS*NUMBER_OF_PROGRAMS];
        IJobDAO jobDAO = persistenceFacade.getNewJobDAO();

        int pIndex = 0;

        for (Program program : programRegistry) {

            Set<Job> jobs = new HashSet<>();

            for (int i = 0; i < NUMBER_OF_JOBS; ++i) {
                int jIndex = pIndex*NUMBER_OF_JOBS + i;

                Job job = new Job();
                job.setState(ExecutionState.RUN);
                job.setCreateTime(Instant.now());

                jobDAO.save(job);
                jobRegistry[jIndex] = job;
                jobs.add(job);
            }

            program.setJobs(jobs);
            programDAO.saveOrUpdate(program);

            ++pIndex;
        }

        // TASKS

        ITaskDAO taskDAO = persistenceFacade.getNewTaskDAO();

        for (Job job : jobRegistry) {

            Set<Task> tasks = new HashSet<>();

            for (int i = 0; i < NUMBER_OF_TASKS; ++i) {

                Task task = new Task();
                task.setState(ExecutionState.SUCCESS);
                task.setCreateTime(Instant.now());
                task.setStartTime(Instant.now());

                taskDAO.save(task);
                tasks.add(task);
            }

            //TODO
            //job.setTasks(tasks);
            jobDAO.saveOrUpdate(job);
        }

        LOGGER.info("Tables were filled!");
    }

    private static <T extends AbstractEntity<U>, U extends Serializable> void setAssociations(
        int numberOfAssociations,
        int maxRegistryIdValue,
        T[] entityRegistry,
        Set<Integer> associationControl,
        Consumer<T> entityConsumer
    ) {
        for (int j = 0; j < numberOfAssociations; ++j) {
            int groupAssociationId;
            do {
                groupAssociationId = ThreadLocalRandom.current().nextInt(0, maxRegistryIdValue);
            } while (associationControl.contains(groupAssociationId));

            associationControl.add(groupAssociationId);
            T associatedEntity = entityRegistry[groupAssociationId];

            entityConsumer.accept(associatedEntity);
        }
    }

    private static <T> T[] getSynchronizedRegistry(
        Function<Integer, T[]> registryConstructor,
        Supplier<Collection<T>> entitySupplier
    ) {
        Collection<T> entityCollection = entitySupplier.get();
        return entityCollection.toArray(registryConstructor.apply(entityCollection.size()));
    }
}
