package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.QueueInfoDTO;

class QueueInfoDTOFormatter extends ShellFormatter<QueueInfoDTO> {

	public QueueInfoDTOFormatter() {
		super(QueueInfoDTO.class);
	}

	@Override
	public String doFormat(QueueInfoDTO queueInfo, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Id: ").append(queueInfo.getId()).append("\n");
		sb.append(shifted).append("Released: ").append(queueInfo.isReleased()).append("\n");
		sb.append(shifted).append("# Tasks: ").append(queueInfo.getNumberOfTasks()).append("\n");

		return sb.toString();
	}
}
