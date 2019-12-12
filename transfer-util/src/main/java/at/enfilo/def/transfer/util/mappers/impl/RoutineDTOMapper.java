package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.FormalParameterDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.util.MapManager;

import java.util.stream.Collectors;


public class RoutineDTOMapper extends AbstractMapper<Routine, RoutineDTO> {

    public RoutineDTOMapper() {
        super(Routine.class, RoutineDTO.class);
    }

    @Override
    public RoutineDTO map(Routine source, RoutineDTO destination)
	throws IllegalArgumentException, IllegalStateException {

    	RoutineDTO dest = destination;
    	if (dest == null) {
			dest = new RoutineDTO();
		}
        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::isPrivateRoutine, dest::setPrivateRoutine);
			mapAttributes(source::getName, dest::setName);
			mapAttributes(source::getDescription, dest::setDescription);
			mapAttributes(source::getRevision, dest::setRevision);
			mapAttributes(source::getType, dest::setType);
			mapAttributes(
				source::getInParameters,
				dest::setInParameters,
				formalParameters -> MapManager.map(formalParameters, FormalParameterDTO.class).collect(Collectors.toList())
			);
			mapAttributes(
				source::getOutParameter,
				dest::setOutParameter,
				formalParameter -> MapManager.map(formalParameter, FormalParameterDTO.class)
			);
			mapAttributes(
				source::getRoutineBinaries,
				dest::setRoutineBinaries,
				routineBinaries -> MapManager.map(routineBinaries, RoutineBinaryDTO.class).collect(Collectors.toSet())
			);
			mapAttributes(
				source::getRequiredFeatures,
				dest::setRequiredFeatures,
				features -> MapManager.map(features, FeatureDTO.class).collect(Collectors.toList())
			);
			mapAttributes(source::getArguments, dest::setArguments);

            return dest;
        }
        return null;
    }
}
