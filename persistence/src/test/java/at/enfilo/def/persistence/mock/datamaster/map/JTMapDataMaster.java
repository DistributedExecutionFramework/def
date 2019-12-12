package at.enfilo.def.persistence.mock.datamaster.map;

import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.domain.map.JTMap;
import at.enfilo.def.domain.map.JTMap_;
import at.enfilo.def.domain.map.pk.JTMapPK;
import at.enfilo.def.persistence.mock.api.IDataMaster;

import java.util.Collection;

/**
 * Created by mase on 05.09.2016.
 */
public class JTMapDataMaster extends MapDataMaster<JTMap, JTMapPK> {

    public JTMapDataMaster(
        IDataMaster<Job, String> jobDataMaster,
        IDataMaster<Task, String> taskDataMaster
    ) {
        super(JTMap.class, JTMapPK.class, JTMap_.id);

        Job job = jobDataMaster.getDummyEntityForSearch();
        Collection<Task> taskSet = taskDataMaster.getAllDummyEntities();

        taskSet.forEach(task -> registerDummyEntity(new JTMap(job, task)));
    }
}
