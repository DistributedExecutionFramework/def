package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.ProgramDTO;

import java.time.Instant;

class ProgramDTOFormatter extends ShellFormatter<ProgramDTO> {

	public ProgramDTOFormatter() {
		super(ProgramDTO.class);
	}

	@Override
	public String doFormat(ProgramDTO program, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Id: ").append(program.getId()).append("\n");
		sb.append(shifted).append("CreateTime: ").append(Instant.ofEpochMilli(program.getCreateTime())).append("\n");
		sb.append(shifted).append("FinishedTime: ").append(Instant.ofEpochMilli(program.getFinishTime())).append("\n");
		sb.append(shifted).append("State: ").append(program.getState()).append("\n");

		return sb.toString();
	}
}
