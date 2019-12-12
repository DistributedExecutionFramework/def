package agentconf.routine;

import agentconf.routine.datatypes.AgentConfCSVFile;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.routine.AccessParameterException;
import at.enfilo.def.routine.ObjectiveRoutine;
import at.enfilo.def.routine.RoutineException;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class NumberVisitsUniqueNewestLevelplotRoutine extends ObjectiveRoutine<AgentConfCSVFile> {

    @Override
    protected AgentConfCSVFile routine() throws RoutineException {
        try {

            AgentConfCSVFile file = getParameter("file", AgentConfCSVFile.class);
            List<String> lines = file.getLines();
            String fileNameWithoutExtension = file.getFilename().split("\\.")[0];
            int pixelWindowWidth = getParameter("pixelWindowWidth", DEFInteger.class).getValue();
            int pixelWindowHeight = getParameter("pixelWindowHeight", DEFInteger.class).getValue();
            int imageWidth = getParameter("imageWidth", DEFInteger.class).getValue();
            int imageHeight = getParameter("imageHeight", DEFInteger.class).getValue();

            Table<Integer, Integer, Integer> visitsCount = HashBasedTable.create();
            for (String line: lines) {
                String[] elements = line.split(";");
                if (elements.length > 0) {
                    try {
                        int xPos = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[1]).intValue();
                        int yPos = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[2]).intValue();

                        if (!(xPos == 0 && yPos == 0)) {
                            int xField = xPos / pixelWindowWidth;
                            int yField = yPos / pixelWindowHeight;

                            if (xPos >= imageWidth) {
                                xField =- 1;
                            }
                            if (yPos >= imageHeight) {
                                yField =- 1;
                            }

                            int xFieldCenter = xField * pixelWindowWidth + pixelWindowWidth/2;
                            int yFieldCenter = yField * pixelWindowHeight + pixelWindowHeight/2;

                            visitsCount.put(xFieldCenter, yFieldCenter, 1);
                        }
                    } catch (ParseException e) {
                        System.out.println(MessageFormat.format("CSV line '{0}' couldn't be parsed.", line));
                    }
                }
            }

            List<String> resultingLines = new LinkedList<>();
            visitsCount.cellSet().forEach(cell -> {
                resultingLines.add(MessageFormat.format(
                        "{0,number,#};{1,number,#};{2,number,#};{3}",
                        cell.getRowKey(), cell.getColumnKey(), cell.getValue(), fileNameWithoutExtension
                ));
            });

            AgentConfCSVFile result = new AgentConfCSVFile();
            result.setLines(resultingLines);
            return result;

        } catch (AccessParameterException e) {
            throw new RoutineException(e);
        }
    }
}
