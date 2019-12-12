package agentconf.routine;

import agentconf.routine.datatypes.AgentConfCSVFile;
import at.enfilo.def.routine.AccessParameterException;
import at.enfilo.def.routine.ObjectiveRoutine;
import at.enfilo.def.routine.RoutineException;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MM43DRoutine extends ObjectiveRoutine<AgentConfCSVFile> {

    @Override
    protected AgentConfCSVFile routine() throws RoutineException {
        try {
            AgentConfCSVFile file = getParameter("file", AgentConfCSVFile.class);
            List<String> lines = file.getLines();
            String fileNameWithoutExtension = file.getFilename().split("\\.")[0];
            int timeOffset = Integer.parseInt(lines.get(0).split(";")[0]);
            List<String> resultingLines = new LinkedList<>();

            for (String line: lines) {
                String[] elements = line.split(";");
                if (elements.length > 0) {
                    try {
                        int milliseconds = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[0]).intValue();
                        int x = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[1]).intValue();
                        int y = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[2]).intValue();
                        resultingLines.add(MessageFormat.format(
                                "{0,number,#};{1,number,#};{2,number,#};{3}",
                                milliseconds - timeOffset, x, y, fileNameWithoutExtension));
                    } catch (ParseException e) {
                        System.out.println(MessageFormat.format("CSV line '{0}' couldn't be parsed.", line));
                    }
                }
            }

            AgentConfCSVFile result = new AgentConfCSVFile();
            result.setLines(resultingLines);
            return result;

        } catch (AccessParameterException e) {
            throw new RoutineException(e);
        }

    }
}
