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

public class NumberVisitsUniqueLevelplotRoutine extends ObjectiveRoutine<AgentConfCSVFile> {

    @Override
    protected AgentConfCSVFile routine() throws RoutineException {
        try {

            List<String> previousResultingLines = getParameter("previousResults", AgentConfCSVFile.class).getLines();
            List<String> newLines = getParameter("newFile", AgentConfCSVFile.class).getLines();

            int pixelWindowWidth = getParameter("pixelWindowWidth", DEFInteger.class).getValue();
            int pixelWindowHeight = getParameter("pixelWindowHeight", DEFInteger.class).getValue();
            int imageWidth = getParameter("imageWidth", DEFInteger.class).getValue();
            int imageHeight = getParameter("imageHeight", DEFInteger.class).getValue();

            Table<Integer, Integer, Integer> visitsCountTotal = HashBasedTable.create();
            for (String line: previousResultingLines) {
                String[] elements = line.split(";");
                if (elements.length > 0) {
                    try {
                        int x = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[0]).intValue();
                        int y = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[1]).intValue();
                        int count = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[2]).intValue();
                        visitsCountTotal.put(x, y, count);
                    } catch (ParseException e) {
                        System.out.println(MessageFormat.format("CSV line '{0}' couldn't be parsed.", line));
                    }
                }
            }

            Table<Integer, Integer, Integer> visitsCountCurrent = HashBasedTable.create();
            for (String line: newLines) {
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

                            visitsCountCurrent.put(xFieldCenter, yFieldCenter, 1);
                        }
                    } catch (ParseException e) {
                        System.out.println(MessageFormat.format("CSV line '{0}' couldn't be parsed.", line));
                    }
                }
            }

            visitsCountCurrent.cellSet().forEach(cell -> {
                if (!visitsCountTotal.contains(cell.getRowKey(), cell.getColumnKey())) {
                    visitsCountTotal.put(cell.getRowKey(), cell.getColumnKey(), 0);
                }
                visitsCountTotal.put(cell.getRowKey(), cell.getColumnKey(), visitsCountTotal.get(cell.getRowKey(), cell.getColumnKey()) + 1);
            });

            List<String> resultingLines = new LinkedList<>();
            visitsCountTotal.cellSet().forEach(cell -> {
                resultingLines.add(MessageFormat.format(
                        "{0,number,#};{1,number,#};{2,number,#}",
                        cell.getRowKey(), cell.getColumnKey(), cell.getValue()
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
