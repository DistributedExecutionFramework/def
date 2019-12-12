package at.enfilo.def.client.shell.formatter;


import at.enfilo.def.transfer.dto.ClusterInfoDTO;

import java.time.Instant;

class ClusterInfoDTOFormatter extends ShellFormatter<ClusterInfoDTO> {

    public ClusterInfoDTOFormatter() {
        super(ClusterInfoDTO.class);
    }

    @Override
	public String doFormat(ClusterInfoDTO info, char[] shifted) {
		StringBuilder clusterInfo = new StringBuilder();

		clusterInfo.append(shifted).append("Id: ").append(info.getId()).append("\n");
		clusterInfo.append(shifted).append("Name: ").append(info.getName()).append("\n");
		clusterInfo.append(shifted).append("Manager-Id: ").append(info.getManagerId()).append("\n");
		clusterInfo.append(shifted).append("Cloud-Type: ").append(info.getCloudType()).append("\n");
		clusterInfo.append(shifted).append("Start-Time: ").append(Instant.ofEpochMilli(info.getStartTime())).append("\n");
		clusterInfo.append(shifted).append("Number of Workers: ").append(info.getNumberOfWorkers()).append("\n");
		clusterInfo.append(shifted).append("Active Programs: ").append(info.getActivePrograms()).append("\n");

		return clusterInfo.toString();
	}
}
