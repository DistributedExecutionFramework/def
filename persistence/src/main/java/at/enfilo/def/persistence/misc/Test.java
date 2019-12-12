package at.enfilo.def.persistence.misc;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.persistence.api.IUserDAO;
import at.enfilo.def.persistence.dao.PersistenceFacade;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mase on 18.10.2016.
 */
public class Test {
    public static void main(String[] args) {

        PersistenceFacade persistenceFacade = new PersistenceFacade();

        User user1 = new User();
        user1.setName("test");
        user1.setPass("super pass");
        user1.setSalt("super salt");

        Group group1 = new Group();
        group1.setName("group 1");
        group1.setDescription("ver nice group");

        Group group2 = new Group();
        group2.setName("group 2");
        group2.setDescription("worst group ever");

        persistenceFacade.getNewGroupDAO().batchSaveOrUpdate(new Group[]{group1, group2});

        IUserDAO userDAO = persistenceFacade.getNewUserDAO();
        userDAO.save(user1);

        //userDAO.addGroups(user1, group1, group2);
        Set<Group> groupSet = new HashSet<>();
        groupSet.add(group1);
        groupSet.add(group2);

        user1.setGroups(groupSet);

        userDAO.saveOrUpdate(user1);

        //List<Group> groupList = userDAO.getGroups(user1);
        user1 = userDAO.findAll().get(0);
        userDAO.forceLoadLazyField(user1, User::getGroups);

        Set<Group> groupList = user1.getGroups();


        for (Group group : groupList) {
            System.out.println(group.getName() + " - " + group.getDescription());
        }

        persistenceFacade.getNewGroupDAO().delete(group2);

        System.out.println("----------after remove-------");


        //List<Group> group1List = userDAO.getGroups(user1);
        user1 = userDAO.findAll().get(0);
        userDAO.forceLoadLazyField(user1, User::getGroups);

        Set<Group> group1List = user1.getGroups();

        for (Group group : group1List) {
            System.out.println(group.getName() + " - " + group.getDescription());
        }

        System.out.println("for breakpoint");
    }
}
