package at.enfilo.def.node.routine.factory.impl;

import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.routine.util.Pipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class PipePlaceholder implements IPlaceholder {

    private static final List<String> KEYWORDS = Collections.unmodifiableList(Arrays.asList("in", "out", "ctrl", "pipes"));

    private String inPipe;
    private String outPipe;
    private String ctrlPipe;

    private String pipes;

    PipePlaceholder(SequenceStep step, boolean addOutPipe) {
        inPipe = resolvePipe(step.getInPipe());
        if (inPipe != null) {
            pipes = inPipe;
        }
        if (addOutPipe) {
            outPipe = resolvePipe(step.getOutPipe());
            if (outPipe != null) {
                if (pipes != null) {
                    pipes += " " + outPipe;
                } else {
                    pipes = outPipe;
                }
            }
        }
        ctrlPipe = resolvePipe(step.getCtrlPipe());
        if (ctrlPipe != null) {
            if (pipes != null) {
                pipes += " " + ctrlPipe;
            } else {
                pipes = ctrlPipe;
            }
        }
    }

    @Override
    public List<String> getKeywords() {
        return KEYWORDS;
    }

    @Override
    public String get(String cmd) {
        switch (cmd) {
            case "in":
                return inPipe;
            case "out":
                return outPipe;
            case "ctrl":
                return ctrlPipe;
            case "pipes":
                return pipes;
        }
        return null;
    }

    /**
     * Helper method that resolves pipe to absolute path.
     *
     * @param pipe - pipe instance to be resolved.
     * @return resolved absolute path to pipe.
     */
    private String resolvePipe(Pipe pipe) {
        return pipe != null ? pipe.resolve().getAbsolutePath() : null;
    }
}
