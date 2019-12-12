package at.enfilo.def.node.routine.factory.impl;

import at.enfilo.def.transfer.dto.RoutineBinaryDTO;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

class RoutineBinaryPlaceholder implements IPlaceholder {

    private static final List<String> KEYWORDS = Collections.unmodifiableList(Arrays.asList("rb", "rbs", "rbp", "rb*"));

    private int counter = 0;
    private boolean accessedPrimary = false;
    private boolean hasPrimary = false;
    private List<String> binaries = new ArrayList<>();

    RoutineBinaryPlaceholder(Set<RoutineBinaryDTO> binaries) throws MalformedURLException {
        this(binaries, true);
    }

    RoutineBinaryPlaceholder(Set<RoutineBinaryDTO> binaries, boolean parseBinaryURL) throws MalformedURLException {
        if (binaries != null) {
            for (RoutineBinaryDTO binary : binaries) {
                if (binary.isPrimary()) {
                    hasPrimary = true;
                    this.binaries.add(resolvePath(binary.getExecutionUrl(), parseBinaryURL));
                }
            }
            for (RoutineBinaryDTO binary : binaries) {
                if (!binary.isPrimary()) {
                    this.binaries.add(resolvePath(binary.getExecutionUrl(), parseBinaryURL));
                }
            }
        }
    }

    public List<String> getKeywords() {
        return KEYWORDS;
    }

    @Override
    public String get(String cmd) {
        switch (cmd) {
            case "rb":
                counter = counter % binaries.size();
                if (accessedPrimary && counter == 0) {
                    counter++;
                }
                return binaries.get(counter++);

            case "rbp":
                if (hasPrimary) {
                    accessedPrimary = true;
                    return binaries.get(0);
                } else {
                    return null;
                }

            case "rbs":
                if (accessedPrimary) {
                    return binaries.size() > 1 ? String.join(" ", binaries.subList(1, binaries.size())) : null;
                } else {
                    return String.join(" ", binaries);
                }
            default:
                if (cmd.matches("rb[0-9]+")) {
                    int idx = Integer.parseInt(cmd.substring(2, cmd.length()));
                    if (accessedPrimary) {
                        idx++;
                    }
                    return binaries.size() > idx ? binaries.get(idx) : null;
                }
        }
        return null;
    }

    private String resolvePath(String url, boolean parseBinaryURL) throws MalformedURLException {
        if (!parseBinaryURL) {
            return url;
        }

        if (!url.startsWith("file:")) {
            url = "file:" + url;
        }
        return new URL(url).getFile();
    }
}
