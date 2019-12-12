package at.enfilo.def.node.routine.factory.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ArgumentsPlaceholder implements IPlaceholder {

    private static final List<String> KEYWORDS = Collections.unmodifiableList(Arrays.asList("arg", "args", "arg*"));

    private int counter = 0;
    private List<String> arguments;

    ArgumentsPlaceholder(List<String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public List<String> getKeywords() {
        return KEYWORDS;
    }

    @Override
    public String get(String cmd) {
        if (arguments == null || arguments.isEmpty()) {
            return null;
        }
        switch (cmd) {
            case "arg":
                counter = counter % arguments.size();
                return arguments.get(counter++);
            case "args":
                return String.join(" ", arguments);
            default:
                if (cmd.matches("arg[0-9]+")) {
                    int idx = Integer.parseInt(cmd.substring(3, cmd.length()));
                    if (arguments.size() > idx) {
                        return arguments.get(idx);
                    }
                }
        }
        return null;
    }
}
