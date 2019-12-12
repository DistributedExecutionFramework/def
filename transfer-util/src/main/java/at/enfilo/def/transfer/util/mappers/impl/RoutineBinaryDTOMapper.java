package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;

/**
 * Created by mase on 06.04.2017.
 */
public class RoutineBinaryDTOMapper extends AbstractMapper<RoutineBinary, RoutineBinaryDTO> {

    public RoutineBinaryDTOMapper() {
        super(RoutineBinary.class, RoutineBinaryDTO.class);
    }

    @Override
    public RoutineBinaryDTO map(RoutineBinary source, RoutineBinaryDTO destination)
    throws IllegalArgumentException, IllegalStateException {

        RoutineBinaryDTO dest = destination;
        if (dest == null) {
            dest = new RoutineBinaryDTO();
        }
        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getName, dest::setName);
            mapAttributes(source::getMd5, dest::setMd5);
            mapAttributes(source::getSizeInBytes, dest::setSizeInBytes);
            mapAttributes(source::isPrimary, dest::setPrimary);
            mapAttributes(source::getUrl, dest::setUrl);
            mapAttributes(source::getExecutionUrl, dest::setExecutionUrl);

            return dest;
        }
        return null;
    }
}
