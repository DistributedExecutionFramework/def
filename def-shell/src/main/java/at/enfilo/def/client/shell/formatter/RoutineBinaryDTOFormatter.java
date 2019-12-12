package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.RoutineBinaryDTO;

class RoutineBinaryDTOFormatter extends ShellFormatter<RoutineBinaryDTO> {

	public RoutineBinaryDTOFormatter() {
		super(RoutineBinaryDTO.class);
	}

	@Override
	public String doFormat(RoutineBinaryDTO routineBinary, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Id: ").append(routineBinary.getId()).append("\n");
		sb.append(shifted).append("Name: ").append(routineBinary.getName()).append("\n");
		sb.append(shifted).append("Md5: ").append(routineBinary.getMd5()).append("\n");
		sb.append(shifted).append("Size (bytes): ").append(routineBinary.getSizeInBytes()).append("\n");
		sb.append(shifted).append("Is primary: ").append(routineBinary.isPrimary()).append("\n");
		sb.append(shifted).append("URL: ").append(routineBinary.getUrl()).append("\n");

		return sb.toString();
	}
}
