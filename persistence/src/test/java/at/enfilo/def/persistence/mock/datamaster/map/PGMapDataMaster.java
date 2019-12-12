package at.enfilo.def.persistence.mock.datamaster.map;

import at.enfilo.def.domain.entity.Group;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.map.PGMap;
import at.enfilo.def.domain.map.PGMap_;
import at.enfilo.def.domain.map.pk.PGMapPK;
import at.enfilo.def.persistence.mock.api.IDataMaster;

import java.util.Collection;

/**
 * Created by mase on 05.09.2016.
 */
public class PGMapDataMaster extends MapDataMaster<PGMap, PGMapPK> {

    private IDataMaster<Program, String> programDataMaster;
    private IDataMaster<Group, String> groupDataMaster;

    public PGMapDataMaster(
        IDataMaster<Program, String> programDataMaster,
        IDataMaster<Group, String> groupDataMaster
    ) {
        super(PGMap.class, PGMapPK.class, PGMap_.id);

        Group group = groupDataMaster.getDummyEntityForSearch();
        Collection<Program> programSet = programDataMaster.getAllDummyEntities();

        programSet.forEach(program -> registerDummyEntity(new PGMap(program, group)));
    }
}
