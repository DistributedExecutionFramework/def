package at.enfilo.def.client.shell.formatter;

import at.enfilo.def.transfer.dto.NodeEnvironmentDTO;

public class NodeEnvironmentDTOFormatter extends ShellFormatter<NodeEnvironmentDTO> {

    public NodeEnvironmentDTOFormatter() {
        super(NodeEnvironmentDTO.class);
    }

    @Override
    public String doFormat(NodeEnvironmentDTO env, char[] shifted) {
        StringBuilder sb = new StringBuilder();

        sb.append(shifted).append("Id: ").append(env.getId()).append("\n");
        sb.append(shifted).append("Environment: ").append("\n");
        if (env.getEnvironment() != null && !env.getEnvironment().isEmpty()) {
            for (String feature : env.getEnvironment()) {
                sb.append(shifted).append(feature).append("\n");
            }
        }
        return sb.toString();
    }
}