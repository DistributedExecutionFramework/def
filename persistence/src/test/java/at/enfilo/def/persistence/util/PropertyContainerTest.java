package at.enfilo.def.persistence.util;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.Tuple;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mase on 02.09.2016.
 */
public class PropertyContainerTest {

    private static final String PROPERTY_NAME = "PROPERTY_NAME";
    private static final String PROPERTY_VALUE = "PROPERTY_VALUE";

    private ITuple<String, String> propertyContainer;

    @Before
    public void prepare() {
        propertyContainer = new Tuple<>(PROPERTY_NAME, PROPERTY_VALUE);
    }

    @Test
    public void getValueTest() {
        String propertyName = propertyContainer.getKey();
        assertEquals(propertyName, PROPERTY_NAME);
    }

    @Test
    public void getPropertyTest() {
        String propertyValue = propertyContainer.getValue();
        assertEquals(propertyValue, PROPERTY_VALUE);
    }
}
