package at.enfilo.def.persistence.mock.datamaster.map;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.map.UGMap;
import at.enfilo.def.domain.map.UGMap_;
import at.enfilo.def.domain.map.pk.UGMapPK;
import at.enfilo.def.domain.map.pk.UGMapPK_;
import at.enfilo.def.persistence.mock.api.IDataMaster;

import java.util.Collection;

/**
 * Created by mase on 05.09.2016.
 */
public class UGMapDataMaster extends MapDataMaster<UGMap, UGMapPK> {

    public UGMapDataMaster(
        IDataMaster<User, String> userDataMaster,
        IDataMaster<Group, String> groupDataMaster
    ) {
        super(UGMap.class, UGMapPK.class, UGMap_.id);

        User user = userDataMaster.getDummyEntityForSearch();
        Collection<Group> groupSet = groupDataMaster.getAllDummyEntities();

        groupSet.forEach(group -> registerDummyEntity(new UGMap(user, group)));
    }
}
