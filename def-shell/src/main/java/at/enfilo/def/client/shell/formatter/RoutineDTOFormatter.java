package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;

class RoutineDTOFormatter extends ShellFormatter<RoutineDTO> {

	public RoutineDTOFormatter() {
		super(RoutineDTO.class);
	}

	@Override
	public String doFormat(RoutineDTO routine, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Id: ").append(routine.getId()).append("\n");
		sb.append(shifted).append("Is private: ").append(routine.isPrivateRoutine()).append("\n");
		sb.append(shifted).append("Name: ").append(routine.getName()).append("\n");
		sb.append(shifted).append("Description: ").append(routine.getDescription()).append("\n");
		sb.append(shifted).append("Revision: ").append(routine.getRevision()).append("\n");
		sb.append(shifted).append("Type: ").append(routine.getType()).append("\n");
		sb.append(shifted).append("Required Features:").append("\n");
		if (routine.getRequiredFeatures() != null) {
			for (FeatureDTO feature : routine.getRequiredFeatures()) {
				sb.append(shifted).append("  ").append(feature.getName()).append(", ").append(feature.getVersion()).append("\n");
				if (feature.getExtensions() != null) {
					for (FeatureDTO ext : feature.getExtensions()) {
						sb.append(shifted).append("    ").append(ext.getName()).append(", ").append(ext.getVersion()).append("\n");
					}
				}
			}
		}

		sb.append(shifted).append("Binaries: ").append("\n");

		if (routine.getRoutineBinaries() != null) {
			for (RoutineBinaryDTO routineBinaryDTO : routine.getRoutineBinaries()) {
				sb.append(shifted).append("-------------").append("\n");
				sb.append(ShellOutputFormatter.format(routineBinaryDTO, shifted.length + 2)).append("\n");
			}
		}

		return sb.toString();
	}
}
