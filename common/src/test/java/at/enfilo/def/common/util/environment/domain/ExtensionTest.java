package at.enfilo.def.common.util.environment.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExtensionTest {

    @Test
    public void buildFromString() {

        String test1 = "numpy";
        String test2 = "numpy()";
        String test3 = "numpy(1)";
        String test4 = "numpy(1,2,>3,<4,5-6)";

        Extension e1 = Extension.buildFromString(test1);
        Extension e2 = Extension.buildFromString(test2);
        Extension e3 = Extension.buildFromString(test3);
        Extension e4 = Extension.buildFromString(test4);

        assertEquals(null, Extension.buildFromString(null));
        assertEquals(null, Extension.buildFromString(""));
        assertEquals("numpy", e1.getName());
        assertEquals(null, e1.getVersion());
        assertEquals("numpy", e2.getName());
        assertEquals(null, e2.getVersion());
        assertEquals("numpy", e3.getName());
        assertEquals("1", e3.getVersion());
        assertEquals("numpy", e4.getName());
        assertEquals("1,2,>3,<4,5-6", e4.getVersion());
    }

    @Test
    public void matches() {
        Extension extension = new Extension("numpy", "1.15.1");

        assertTrue(extension.matches(null));

        Extension test1 = new Extension();
        Extension test2 = new Extension("matplotlib");
        Extension test3 = new Extension("numpy");
        Extension test4 = new Extension("numpy", "1.15.1");
        Extension test5 = new Extension("numpy", ">1");
        Extension test6 = new Extension("numpy", "<1");

        assertTrue(extension.matches(test1));
        assertFalse(extension.matches(test2));
        assertTrue(extension.matches(test3));
        assertTrue(extension.matches(test4));
        assertTrue(extension.matches(test5));
        assertFalse(extension.matches(test6));
    }
}
