package at.enfilo.def.persistence.dao;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Group_;
import at.enfilo.def.persistence.api.IGroupDAO;

/**
 * Created by mase on 16.08.2016.
 */
class GroupDAO extends GenericDAO<Group, String> implements IGroupDAO {

    public GroupDAO() {
        super(Group.class, String.class, Group_.id);
    }

}
