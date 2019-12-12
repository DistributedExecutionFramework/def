package at.enfilo.def.agentconf.routine;

import at.enfilo.def.agentconf.routine.datatypes.CSVFile;
import at.enfilo.def.agentconf.routine.datatypes.CoordinateCount;
import at.enfilo.def.agentconf.routine.datatypes.PixelCount;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.routine.AccessParameterException;
import at.enfilo.def.routine.ObjectiveRoutine;
import at.enfilo.def.routine.RoutineException;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.LinkedList;
import java.util.List;

public class AgentConfDemoRoutine extends ObjectiveRoutine<PixelCount> {

    @Override
    protected PixelCount routine() throws RoutineException {
        try {

            List<String> csvLines = getParameter("file", CSVFile.class).getLines();
            int nrOfRows = getParameter("nrOfRows", DEFInteger.class).getValue();
            int nrOfColumns = getParameter("nrOfColumns", DEFInteger.class).getValue();

            Table<Integer, Integer, Integer> pixelsCount = HashBasedTable.create();

            for (String line: csvLines) {
                String[] elements = line.split(";");
                int x = Integer.parseInt(elements[1]);
                int y = Integer.parseInt(elements[2]);
                if (!pixelsCount.contains(y, x)) {
                    pixelsCount.put(y, x, 0);
                }
                pixelsCount.put(y, x, pixelsCount.get(y, x) + 1);
            }

            List<CoordinateCount> coordinateCounts = new LinkedList<>();
            pixelsCount.cellSet().forEach(cell -> {
                if (cell.getValue() != null && cell.getValue() != 0) {
                    coordinateCounts.add(new CoordinateCount(
                            cell.getColumnKey().shortValue(),
                            cell.getRowKey().shortValue(),
                            cell.getValue().shortValue()
                    ));
                }
            });

            return new PixelCount(coordinateCounts, (short)nrOfColumns, (short)nrOfRows);

        } catch (AccessParameterException e) {
            throw new RoutineException(e);
        }
    }
}
