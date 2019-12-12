package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.transfer.util.mappers.impl.AbstractMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mase on 29.08.2016.
 */
public class MapAttributeTest {

    @Test
    public void mapAttributes() {

        String sourceString = "value for destination";
        StringBuilder destinationStringBuilder = new StringBuilder();

        AbstractMapper.mapAttributes(sourceString::toString, destinationStringBuilder::append);

        assertEquals(sourceString, destinationStringBuilder.toString());
    }

    @Test
    public void mapAttributesWithMapper() {
        int sourceValue = 351263617;
        StringBuilder destinationStringBuilder = new StringBuilder();

        AbstractMapper.mapAttributes(() -> sourceValue, destinationStringBuilder::append, String::valueOf);

        assertEquals(String.valueOf(sourceValue), destinationStringBuilder.toString());
    }
}
