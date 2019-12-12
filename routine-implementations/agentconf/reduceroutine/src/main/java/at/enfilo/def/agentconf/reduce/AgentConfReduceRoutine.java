package at.enfilo.def.agentconf.reduce;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.Tuple;
import at.enfilo.def.routine.ReduceRoutine;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AgentConfReduceRoutine extends ReduceRoutine<PixelCount> {
    private final Map<String, Table<Integer, Integer, Integer>> results;
    private short maxX = -1;
    private short maxY = -1;

    public AgentConfReduceRoutine() {
        super(PixelCount.class);
        results = new HashMap<>();
    }

    @Override
    protected void reduceValue(String key, PixelCount pixelCount) {
        if (maxX > 0 && maxY > 0 && (pixelCount.getMaxX() != maxX || pixelCount.getMaxY() != maxY)) {
            throw new IllegalArgumentException("The maxX and maxY values must be the same for all reduce inputs");
        }

        if (!results.containsKey(key)) {
            results.put(key, HashBasedTable.create());
        }

        Table<Integer, Integer, Integer> table = results.get(key);
        for (CoordinateCount c: pixelCount.getCountList()) {
            int value = 0;
            if (table.contains((int)c.y, (int)c.x)) {
                value = table.get((int)c.y, (int)c.x);
            }
            table.put((int)c.y, (int)c.x, value + c.getCount());
        }
    }

    @Override
    protected List<ITuple<String, PixelCount>> finalizeReduce() {
        List<ITuple<String, PixelCount>> rv = new LinkedList<>();

        results.entrySet().forEach(
                entry -> {
                    List<CoordinateCount> coordinateCounts = new LinkedList<>();
                    entry.getValue().cellSet().forEach(cell -> {
                        if (cell.getValue() != null && cell.getValue() != 0) {
                            coordinateCounts.add(new CoordinateCount(
                                    cell.getColumnKey().shortValue(),
                                    cell.getRowKey().shortValue(),
                                    cell.getValue().shortValue()
                            ));
                        }
                    });
                    rv.add(new Tuple<>(entry.getKey(), new PixelCount(coordinateCounts, maxX, maxY)));
                }
        );
        return rv;
    }
}
