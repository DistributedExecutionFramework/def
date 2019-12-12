package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.transfer.util.mappers.impl.MapperTuple;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mase on 29.08.2016.
 */
public class MapperTupleTest {

    private static final Class<String> stringClass = String.class;
    private static final Class<Integer> integerClass = Integer.class;
    private static final Class<Double> doubleClass = Double.class;

    private MapperTuple<String, Integer> firstMapperTuple;
    private MapperTuple<String, Integer> secondMapperTuple;
    private MapperTuple<String, Double> thirdMapperTuple;

    @Before
    public void prepare() {
        firstMapperTuple = MapperTuple.wrap(stringClass, integerClass);
        secondMapperTuple = MapperTuple.wrap(stringClass, integerClass);
        thirdMapperTuple = MapperTuple.wrap(stringClass, doubleClass);
    }

    @Test
    @SuppressWarnings("EqualsWithItself")
    public void selfEquals() {
        boolean result = firstMapperTuple.equals(firstMapperTuple);
        assertTrue(result);
    }

    @Test
    public void mirrorEquals() {
        boolean result = firstMapperTuple.equals(secondMapperTuple);
        assertTrue(result);
    }

    @Test
    @SuppressWarnings("EqualsBetweenInmapibleTypes")
    public void notEqualsUnsupported() {
        boolean result = firstMapperTuple.equals(thirdMapperTuple);
        assertFalse(result);
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void notEqualsNull() {
        boolean result = firstMapperTuple.equals(null);
        assertFalse(result);
    }

    @Test
    public void sourceClassEquals() {
        MapperTuple<?, ?> testTuple = firstMapperTuple;
        boolean result = testTuple.getKey().equals(stringClass);

        assertTrue(result);
    }

    @Test
    public void destinationClassEquals() {
        MapperTuple<?, ?> testTuple = firstMapperTuple;
        boolean result = testTuple.getValue().equals(integerClass);

        assertTrue(result);
    }
}
