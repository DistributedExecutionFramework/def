package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;

/**
 * Created by mase on 06.04.2017.
 */
public class RoutineBinaryMapper extends AbstractMapper<RoutineBinaryDTO, RoutineBinary> {

    public RoutineBinaryMapper() {
        super(RoutineBinaryDTO.class, RoutineBinary.class);
    }

    @Override
    public RoutineBinary map(RoutineBinaryDTO source, RoutineBinary destination)
	throws IllegalArgumentException, IllegalStateException {

    	RoutineBinary dest = destination;
    	if (dest == null) {
			dest = new RoutineBinary();
		}
        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getMd5, dest::setMd5);
			mapAttributes(source::getSizeInBytes, dest::setSizeInBytes);
			mapAttributes(source::isPrimary, dest::setPrimary);
			mapAttributes(source::getUrl, dest::setUrl);
			mapAttributes(source::getData, dest::setData);

            return dest;
        }
        return null;
    }
}
