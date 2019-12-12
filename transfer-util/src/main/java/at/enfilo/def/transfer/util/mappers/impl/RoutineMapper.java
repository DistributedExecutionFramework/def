package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.FormalParameter;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.entity.RoutineBinary;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.util.MapManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class RoutineMapper extends AbstractMapper<RoutineDTO, Routine> {

    public RoutineMapper() {
        super(RoutineDTO.class, Routine.class);
    }

    @Override
    public Routine map(RoutineDTO source, Routine destination)
	throws IllegalArgumentException, IllegalStateException {

    	Routine dest = destination;
    	if (dest == null) {
			dest = new Routine();
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
				formalParameterDTOS -> MapManager.map(formalParameterDTOS, FormalParameter.class).collect(Collectors.toList())
			);
			mapAttributes(
				source::getOutParameter,
				dest::setOutParameter,
				formalParameterDTO -> MapManager.map(formalParameterDTO, FormalParameter.class)
			);
			mapAttributes(
				source::getRoutineBinaries,
				dest::setRoutineBinaries,
				routineBinaryDTOS -> MapManager.map(routineBinaryDTOS, RoutineBinary.class).collect(Collectors.toSet())
			);
			mapAttributes(
					source::getRequiredFeatures,
					dest::setRequiredFeatures,
					features -> MapManager.map(features, Feature.class).collect(Collectors.toSet())
			);
			mapAttributes(source::getArguments, dest::setArguments);

            return dest;
        }
        return null;
    }
}
