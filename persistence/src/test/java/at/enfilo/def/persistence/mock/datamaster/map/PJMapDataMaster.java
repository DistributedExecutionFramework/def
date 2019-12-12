package at.enfilo.def.persistence.mock.datamaster.map;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.map.PJMap;
import at.enfilo.def.domain.map.PJMap_;
import at.enfilo.def.domain.map.pk.PJMapPK;
import at.enfilo.def.persistence.mock.api.IDataMaster;

import java.util.Collection;

/**
 * Created by mase on 05.09.2016.
 */
public class PJMapDataMaster extends MapDataMaster<PJMap, PJMapPK> {

    public PJMapDataMaster(
        IDataMaster<Program, String> programDataMaster,
        IDataMaster<Job, String> jobDataMaster
    ) {
        super(PJMap.class, PJMapPK.class, PJMap_.id);

        Program program = programDataMaster.getDummyEntityForSearch();
        Collection<Job> jobSet = jobDataMaster.getAllDummyEntities();

        jobSet.forEach(job -> registerDummyEntity(new PJMap(program, job)));
    }
}
