package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.ResourceDTO;

class ResourceDTOFormatter extends ShellFormatter<ResourceDTO> {

    public ResourceDTOFormatter() {
        super(ResourceDTO.class);
    }

    @Override
	public String doFormat(ResourceDTO resource, char[] shifted) {
		StringBuilder sb = new StringBuilder();

		sb.append(shifted).append("Id: ").append(resource.getId()).append("\n");
		sb.append(shifted).append("Key: ").append(resource.getKey()).append("\n");
		sb.append(shifted).append("DataTypeId: ").append(resource.getDataTypeId()).append("\n");
		sb.append(shifted).append("Data: ").append(resource.data == null ? "-" : resource.data.array().length).append(" bytes\n");
		sb.append(shifted).append("URL: ").append(resource.getUrl()).append("\n");

		return sb.toString();
	}
}
