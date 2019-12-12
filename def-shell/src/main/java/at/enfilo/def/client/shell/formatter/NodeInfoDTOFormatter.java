package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.NodeInfoDTO;

import java.time.Instant;
import java.util.Map;

class NodeInfoDTOFormatter extends ShellFormatter<NodeInfoDTO> {

	public NodeInfoDTOFormatter() {
		super(NodeInfoDTO.class);
	}

	@Override
	public String doFormat(NodeInfoDTO info, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Id: ").append(info.getId()).append("\n");
		sb.append(shifted).append("Timestamp: ").append(Instant.ofEpochMilli(info.getTimeStamp())).append("\n");
		sb.append(shifted).append("Cluster-Id: ").append(info.getClusterId()).append("\n");
		sb.append(shifted).append("Type: ").append(info.getType()).append("\n");
		sb.append(shifted).append("Number of Cores: ").append(info.getNumberOfCores()).append("\n");
		sb.append(shifted).append("Load: ").append(info.getLoad()).append("\n");
		if (info.getParameters() != null) {
			for (Map.Entry<String, String> param : info.getParameters().entrySet()) {
				sb.append(shifted).append(param.getKey()).append(": ").append(param.getValue()).append("\n");
			}
		}

		return sb.toString();
	}
}
