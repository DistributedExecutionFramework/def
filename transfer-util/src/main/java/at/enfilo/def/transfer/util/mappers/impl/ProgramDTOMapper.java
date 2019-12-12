package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.time.Instant;

/**
 * Created by mase on 26.08.2016.
 */
public class ProgramDTOMapper extends AbstractMapper<Program, ProgramDTO> {

    public ProgramDTOMapper() {
        super(Program.class, ProgramDTO.class);
    }

    @Override
    public ProgramDTO map(Program source, ProgramDTO destination)
    throws IllegalArgumentException, IllegalStateException {

        ProgramDTO dest = destination;
        if (dest == null) {
            dest = new ProgramDTO();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getState, dest::setState);
            mapAttributes(source::getCreateTime, dest::setCreateTime, Instant::toEpochMilli);
            mapAttributes(source::getFinishTime, dest::setFinishTime, Instant::toEpochMilli);
            mapAttributes(source::isMasterLibraryRoutine, dest::setMasterLibraryRoutine);
            mapAttributes(source::getName, dest::setName);
            mapAttributes(source::getDescription, dest::setDescription);
            mapAttributes(source.getJobs()::size, dest::setNrOfJobs);
            mapAttributes(source.getOwner()::getId, dest::setUserId);
            return dest;
        }
        return null;
    }
}
