package at.enfilo.def.persistence.misc;

import at.enfilo.def.common.util.DEFHashHelper;
import at.enfilo.def.domain.entity.AbstractEntity;
import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.persistence.api.IGroupDAO;
import at.enfilo.def.persistence.api.IPersistenceFacade;
import at.enfilo.def.persistence.api.IUserDAO;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import at.enfilo.def.persistence.util.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
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
