package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;

class ServiceEndpointDTOFormatter extends ShellFormatter<ServiceEndpointDTO> {

	public ServiceEndpointDTOFormatter() {
		super(ServiceEndpointDTO.class);
	}

	@Override
	public String doFormat(ServiceEndpointDTO serviceEndpoint, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Host: ").append(serviceEndpoint.getHost()).append("\n");
		sb.append(shifted).append("Port: ").append(serviceEndpoint.getPort()).append("\n");
		sb.append(shifted).append("Protocol: ").append(serviceEndpoint.getProtocol()).append("\n");
		sb.append(shifted).append("URL-Pattern: ").append(serviceEndpoint.getPathPrefix()).append("\n");

		return sb.toString();
	}
}
