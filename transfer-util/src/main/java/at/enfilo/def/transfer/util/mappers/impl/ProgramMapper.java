package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.time.Instant;

/**
 * Created by mase on 26.08.2016.
 */
public class ProgramMapper extends AbstractMapper<ProgramDTO, Program> {

    public ProgramMapper() {
        super(ProgramDTO.class, Program.class);
    }

    @Override
    public Program map(ProgramDTO source, Program destination)
    throws IllegalArgumentException, IllegalStateException {

        Program dest = destination;
        if (dest == null) {
            dest = new Program();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getState, dest::setState);
            mapAttributes(source::getCreateTime, dest::setCreateTime, Instant::ofEpochMilli);
            mapAttributes(source::getFinishTime, dest::setFinishTime, Instant::ofEpochMilli);
            mapAttributes(source::isMasterLibraryRoutine, dest::setMasterLibraryRoutine);
            mapAttributes(source::getName, dest::setName);
            mapAttributes(source::getDescription, dest::setDescription);

            return dest;
        }
        return null;
    }
}
