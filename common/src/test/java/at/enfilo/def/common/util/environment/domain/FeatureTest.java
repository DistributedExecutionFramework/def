package at.enfilo.def.common.util.environment.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class FeatureTest {

    @Test
    public void addExtension() {
        Feature feature = new Feature();
        Feature feature2 = new Feature("java", "1.8");
        List<Extension> extensions = new ArrayList<>();
        extensions.add(new Extension());
        feature2.setExtensions(extensions);

        assertEquals(0, feature.getExtensions().size());
        assertEquals(1, feature2.getExtensions().size());

        feature.addExtension(new Extension());
        feature2.addExtension(new Extension());

        assertEquals(1, feature.getExtensions().size());
        assertEquals(2, feature2.getExtensions().size());

        feature.addExtension(new Extension());
        feature2.addExtension(new Extension());

        assertEquals(2, feature.getExtensions().size());
        assertEquals(3, feature2.getExtensions().size());
    }

    @Test
    public void stringify() {
        Feature test1 = new Feature("python", "3");
        Feature test2 = new Feature("python");
        Feature test3 = new Feature("python", "1,2,3-4,>5,<6");
        Feature test4 = new Feature();

        Feature test5 = new Feature("python", "3");
        List<Extension> extensions = new ArrayList<>();
        extensions.add(new Extension("numpy"));
        extensions.add(new Extension("numpy", "1.15.1"));
        extensions.add(new Extension("matplotlib", "1,2,3-4,>5,<6"));
        test5.setExtensions(extensions);

        assertEquals("python(3)", test1.toString());
        assertEquals("python", test2.toString());
        assertEquals("python(1,2,3-4,>5,<6)", test3.toString());
        assertEquals(null, test4.toString());
        assertEquals("python(3):numpy,numpy(1.15.1),matplotlib(1,2,3-4,>5,<6)", test5.toString());
    }

    @Test
    @SuppressWarnings("all")
    public void buildFromString() {

        String test1 = "python";
        String test2 = "python()";
        String test3 = "python(3)";
        String test4 = "python(1,2,3-4,>5,<6)";
        String test5 = "python(3):numpy";
        String test6 = "python(3):numpy(1.15.1)";
        String test7 = "python(3):numpy(1.15.1),matplotlib";
        String test8 = "python(3):numpy,matplotlib(1,2,3-4,>5,<6)";

        assertEquals(null, Feature.buildFromString(""));

        Feature feature1 = Feature.buildFromString(test1);
        Feature feature2 = Feature.buildFromString(test2);
        Feature feature3 = Feature.buildFromString(test3);
        Feature feature4 = Feature.buildFromString(test4);
        Feature feature5 = Feature.buildFromString(test5);
        Feature feature6 = Feature.buildFromString(test6);
        Feature feature7 = Feature.buildFromString(test7);
        Feature feature8 = Feature.buildFromString(test8);

        assertEquals("python", feature1.getName());
        assertEquals(null, feature1.getVersion());

        assertEquals("python", feature2.getName());
        assertEquals(null, feature2.getVersion());

        assertEquals("python", feature3.getName());
        assertEquals("3", feature3.getVersion());

        assertEquals("python", feature4.getName());
        assertEquals("1,2,3-4,>5,<6", feature4.getVersion());

        assertEquals("python", feature5.getName());
        assertEquals("3", feature5.getVersion());
        assertEquals(1, feature5.getExtensions().size());
        assertEquals("numpy", feature5.getExtensions().get(0).getName());
        assertEquals(null, feature5.getExtensions().get(0).getVersion());

        assertEquals("python", feature6.getName());
        assertEquals("3", feature6.getVersion());
        assertEquals("numpy", feature6.getExtensions().get(0).getName());
        assertEquals("1.15.1", feature6.getExtensions().get(0).getVersion());

        assertEquals("python", feature7.getName());
        assertEquals("3", feature7.getVersion());
        assertEquals("numpy", feature7.getExtensions().get(0).getName());
        assertEquals("1.15.1", feature7.getExtensions().get(0).getVersion());
        assertEquals("matplotlib", feature7.getExtensions().get(1).getName());
        assertEquals(null, feature7.getExtensions().get(1).getVersion());

        assertEquals("python", feature8.getName());
        assertEquals("3", feature8.getVersion());
        assertEquals("numpy", feature8.getExtensions().get(0).getName());
        assertEquals(null, feature8.getExtensions().get(0).getVersion());
        assertEquals("matplotlib", feature8.getExtensions().get(1).getName());
        assertEquals("1,2,3-4,>5,<6", feature8.getExtensions().get(1).getVersion());
    }

    @Test
    public void matches() {
        Feature feature1 = new Feature();
        Feature feature2 = new Feature("python");
        Feature feature3 = new Feature("python", "3.7");
        Feature feature4 = new Feature("python", "3.7");
        List<Extension> extensions = new ArrayList<>();
        extensions.add(new Extension("numpy", "1.15"));
        extensions.add(new Extension("matplotlib", "2"));
        feature4.setExtensions(extensions);

        Feature test1 = new Feature();
        Feature test2 = new Feature("python");
        Feature test3 = new Feature("python", ">3");
        Feature test4 = new Feature("python", "3.7,4");
        Feature test5 = new Feature("java");
        Feature test6 = new Feature("python", "2.7");

        assertTrue(feature1.matches(test1));
        assertFalse(feature1.matches(test2));
        assertTrue(feature2.matches(test1));
        assertTrue(feature2.matches(test2));
        assertFalse(feature2.matches(test3));
        assertTrue(feature3.matches(test2));
        assertTrue(feature3.matches(test3));
        assertTrue(feature3.matches(test4));
        assertFalse(feature3.matches(test5));
        assertFalse(feature3.matches(test6));
        assertTrue(feature4.matches(test3));

        List<Extension> exTest1 = new ArrayList<>();
        exTest1.add(new Extension("numpy"));
        test3.setExtensions(exTest1);

        assertTrue(feature4.matches(test3));

        exTest1.add(new Extension("matplotlib", ">2"));

        assertTrue(feature4.matches(test3));

        exTest1.clear();
        exTest1.add(new Extension("opencv"));

        assertFalse(feature4.matches(test3));

        exTest1.clear();
        exTest1.add(new Extension("numpy", "1.17"));

        assertFalse(feature4.matches(test3));
    }

    @Test
    public void getMatchingExtension() {
        Feature feature = new Feature("python", "3");
        List<Extension> extensions = new ArrayList<>();
        extensions.add(new Extension("numpy", "1.15.1"));
        extensions.add(new Extension("matplotlib", "2"));
        feature.setExtensions(extensions);

        Feature feature2 = new Feature("python", "3");

        Extension test1 = new Extension();
        Extension test2 = new Extension("");
        Extension test3 = new Extension("numpy");
        Extension test4 = new Extension("numpy", "1.15.1");
        Extension test5 = new Extension("numpy", ">1");
        Extension test6 = new Extension("numpy", "<1");
        Extension test7 = new Extension("test");

        assertEquals(2, feature.getMatching(null).size());
        assertEquals(2, feature.getMatching(test1).size());
        assertEquals(2, feature.getMatching(test2).size());
        assertEquals(1, feature.getMatching(test3).size());
        assertEquals(1, feature.getMatching(test4).size());
        assertEquals(1, feature.getMatching(test5).size());
        assertEquals(0, feature.getMatching(test6).size());
        assertEquals(0, feature.getMatching(test7).size());
        assertEquals(0, feature2.getMatching(null).size());
        assertEquals(0, feature2.getMatching(test3).size());

        feature.addExtension(new Extension("numpy", "1.14"));

        assertEquals(2, feature.getMatching(test3).size());
    }

    @Test
    public void getName() {
        String test1 = "java";
        String test2 = "java(1.8)";
        String test3 = "java(1.8,>1.9,<1.10,1.8-1.10)";
        String test4 = "python(3):numpy";
        String test5 = "python(3):numpy(2),matplotlib";

        assertEquals(null, Feature.getName(null));
        assertEquals(null, Feature.getName(""));
        assertEquals("java", Feature.getName(test1));
        assertEquals("java", Feature.getName(test2));
        assertEquals("java", Feature.getName(test3));
        assertEquals("python", Feature.getName(test4));
        assertEquals("python", Feature.getName(test5));
    }
}
