package at.enfilo.def.common.util.environment.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnvironmentTest {

    @Test
    public void environment() {

        Environment environment1 = new Environment(null);
        Environment environment2 = new Environment(new ArrayList<>());
        Environment environment3 = new Environment();

        assertEquals(0, environment1.getFeatures().size());
        assertEquals(0, environment2.getFeatures().size());
        assertEquals(0, environment3.getFeatures().size());
        assertEquals(0, environment1.getFeatureMap().size());
        assertEquals(0, environment2.getFeatureMap().size());
        assertEquals(0, environment3.getFeatureMap().size());

        List<Feature> features = new ArrayList<>();
        features.add(new Feature("java"));
        features.add(new Feature("java"));
        features.add(new Feature("python"));

        Environment environment = new Environment(features);
        assertEquals(3, environment.getFeatures().size());

        Map<String,List<Integer>> indices = environment.getFeatureMap();
        assertTrue(indices.containsKey("java"));
        assertTrue(indices.containsKey("python"));
        assertEquals(new Integer(0), indices.get("java").get(0));
        assertEquals(new Integer(1), indices.get("java").get(1));
        assertEquals(new Integer(2), indices.get("python").get(0));
    }

    @Test
    public void addFeature() {
        Environment environment = new Environment();

        assertEquals(0, environment.getFeatures().size());
        environment.addFeature(new Feature("java"));
        assertEquals(1, environment.getFeatures().size());
        environment.addFeature(new Feature("java"));
        assertEquals(2, environment.getFeatures().size());
        environment.addFeature(new Feature("python"));
        assertEquals(3, environment.getFeatures().size());

        Map<String,List<Integer>> indices = environment.getFeatureMap();
        assertTrue(indices.containsKey("java"));
        assertTrue(indices.containsKey("python"));
        assertEquals(new Integer(0), indices.get("java").get(0));
        assertEquals(new Integer(1), indices.get("java").get(1));
        assertEquals(new Integer(2), indices.get("python").get(0));
    }

    @Test
    public void getAsString() {
        List<Feature> features = new ArrayList<>();
        features.add(Feature.buildFromString("java"));
        features.add(Feature.buildFromString("java()"));
        features.add(Feature.buildFromString("java(1.8)"));
        features.add(Feature.buildFromString("python(<3.7):numpy,matplotlib(1,2-3)"));

        List<String> environment = new Environment(features).getAsString();

        assertEquals("java", environment.get(0));
        assertEquals("java", environment.get(1));
        assertEquals("java(1.8)", environment.get(2));
        assertEquals("python(<3.7):numpy,matplotlib(1,2-3)", environment.get(3));
    }

    @Test
    public void buildFromString() {
        List<String> features = new ArrayList<>();
        features.add("java");
        features.add("java()");
        features.add("java(1.8)");
        features.add("java(1,2,3-4,>5,<6)");
        features.add("");
        features.add("python(3):numpy");
        features.add("python(3):numpy(1.15.1)");
        features.add("python(3):numpy(1.15.1),matplotlib");
        features.add("python(3):numpy,matplotlib(1,2,3-4,>5,<6)");

        Environment environment1 = Environment.buildFromString(null);
        Environment environment2 = Environment.buildFromString(new ArrayList<>());
        Environment environment3 = Environment.buildFromString(features);

        assertEquals(0, environment1.getFeatures().size());
        assertEquals(0, environment2.getFeatures().size());
        assertEquals(0, environment1.getFeatureMap().size());
        assertEquals(0, environment2.getFeatureMap().size());

        assertEquals(8, environment3.getFeatures().size());
        assertEquals(2, environment3.getFeatureMap().size());

        assertTrue(environment3.getFeatureMap().containsKey("java"));
        assertTrue(environment3.getFeatureMap().containsKey("python"));
        assertEquals(4, environment3.getFeatureMap().get("java").size());
        assertEquals(4, environment3.getFeatureMap().get("python").size());
        assertEquals(new Integer(0), environment3.getFeatureMap().get("java").get(0));
        assertEquals(new Integer(1), environment3.getFeatureMap().get("java").get(1));
        assertEquals(new Integer(2), environment3.getFeatureMap().get("java").get(2));
        assertEquals(new Integer(3), environment3.getFeatureMap().get("java").get(3));
        assertEquals(new Integer(4), environment3.getFeatureMap().get("python").get(0));
        assertEquals(new Integer(5), environment3.getFeatureMap().get("python").get(1));
        assertEquals(new Integer(6), environment3.getFeatureMap().get("python").get(2));
        assertEquals(new Integer(7), environment3.getFeatureMap().get("python").get(3));

        assertEquals("java", environment3.getFeatures().get(0).getName());
        assertEquals("java", environment3.getFeatures().get(1).getName());
        assertEquals("java", environment3.getFeatures().get(2).getName());
        assertEquals("java", environment3.getFeatures().get(3).getName());
        assertEquals("python", environment3.getFeatures().get(4).getName());
        assertEquals("python", environment3.getFeatures().get(5).getName());
        assertEquals("python", environment3.getFeatures().get(6).getName());
        assertEquals("python", environment3.getFeatures().get(7).getName());

        assertEquals(null, environment3.getFeatures().get(0).getVersion());
        assertEquals(null, environment3.getFeatures().get(1).getVersion());
        assertEquals("1.8", environment3.getFeatures().get(2).getVersion());
        assertEquals("1,2,3-4,>5,<6", environment3.getFeatures().get(3).getVersion());
        assertEquals("3", environment3.getFeatures().get(4).getVersion());
        assertEquals("3", environment3.getFeatures().get(5).getVersion());
        assertEquals("3", environment3.getFeatures().get(6).getVersion());
        assertEquals("3", environment3.getFeatures().get(7).getVersion());
    }

    @Test
    public void matchesFeatureList() {
        List<Feature> test1 = new ArrayList<>();
        List<Feature> test2 = new ArrayList<>();
        test2.add(null);
        List<Feature> test3 = new ArrayList<>();
        test3.add(new Feature("java"));
        test3.add(new Feature("java", ">1"));
        test3.add(null);
        test3.add(new Feature("python", "3.7"));
        List<Feature> test4 = new ArrayList<>();
        test4.add(new Feature("csharp", "6"));
        test4.add(new Feature("java"));
        List<Feature> test5 = new ArrayList<>();
        test5.add(new Feature("java"));
        test5.add(new Feature("csharp", "6"));
        List<Feature> test6 = new ArrayList<>();
        Feature f = new Feature("python", ">3");
        f.addExtension(new Extension("numpy", "1.15.1"));
        f.addExtension(new Extension("matplotlib"));
        test6.add(f);

        Environment environment = new Environment();
        environment.addFeature(new Feature("java", "1.8"));
        Feature feature = new Feature("python", "3.7");
        feature.addExtension(new Extension("numpy", "1.15.1"));
        feature.addExtension(new Extension("matplotlib", "2"));
        environment.addFeature(feature);

        Environment environment2 = new Environment();

        assertTrue(environment.matches(test1));
        assertTrue(environment.matches(test2));
        assertTrue(environment.matches(test3));
        assertFalse(environment.matches(test4));
        assertFalse(environment.matches(test5));
        assertTrue(environment.matches(test6));

        assertTrue(environment2.matches(test1));
        assertTrue(environment2.matches(test2));
        assertFalse(environment2.matches(test3));
        assertFalse(environment2.matches(test4));
        assertFalse(environment2.matches(test5));
        assertFalse(environment2.matches(test6));
    }

    @Test
    public void matchesEnvironment() {
        Environment test1 = new Environment();
        List<String> f1 = new ArrayList<>();
        f1.add("java");
        f1.add("java(>1)");
        f1.add(null);
        f1.add("python(3.7)");
        Environment test2 = Environment.buildFromString(f1);

        List<String> f2 = new ArrayList<>();
        f2.add("csharp(6)");
        f2.add("java");
        Environment test3 = Environment.buildFromString(f2);

        List<String> f3 = new ArrayList<>();
        f3.add("java");
        f3.add("csharp(6)");
        Environment test4 = Environment.buildFromString(f3);

        List<String> f4 = new ArrayList<>();
        f4.add("python(>3):numpy(1.15.1),matplotlib");
        Environment test5 = Environment.buildFromString(f4);

        Environment environment = new Environment();
        environment.addFeature(new Feature("java", "1.8"));
        Feature feature = new Feature("python", "3.7");
        feature.addExtension(new Extension("numpy", "1.15.1"));
        feature.addExtension(new Extension("matplotlib", "2"));
        environment.addFeature(feature);

        Environment environment2 = new Environment();

        assertTrue(environment.matches(test1));
        assertTrue(environment.matches(test2));
        assertFalse(environment.matches(test3));
        assertFalse(environment.matches(test4));
        assertTrue(environment.matches(test5));

        assertTrue(environment2.matches(test1));
        assertFalse(environment2.matches(test2));
        assertFalse(environment2.matches(test3));
        assertFalse(environment2.matches(test4));
        assertFalse(environment2.matches(test5));
    }

    @Test
    public void getMatching() {
        Feature test1 = new Feature();
        Feature test2 = new Feature("java");
        Feature test3 = new Feature("java", "1.8");
        Feature test4 = new Feature("java", "1.7,1.8");
        Feature test5 = new Feature("java", ">1.8");
        Feature test6 = Feature.buildFromString("python(3.7):numpy");
        Feature test7 = Feature.buildFromString("python:numpy,matplotlib(>2)");

        Environment environment = new Environment();
        environment.addFeature(new Feature("java", "1.8"));
        environment.addFeature(new Feature("java", "1.7"));
        environment.addFeature(new Feature("python", "3.7"));

        Environment environment2 = new Environment();
        environment2.addFeature(Feature.buildFromString("python(3.7):numpy(1.15.1),matplotlib(2)"));

        List<Feature> result1 = environment.getMatching(test1);
        List<Feature> result2 = environment.getMatching(test2);
        List<Feature> result3 = environment.getMatching(test3);
        List<Feature> result4 = environment.getMatching(test4);
        List<Feature> result5 = environment.getMatching(test5);
        List<Feature> result6 = environment.getMatching(test6);
        List<Feature> result7 = environment.getMatching(test7);
        List<Feature> result8 = environment2.getMatching(test1);
        List<Feature> result9 = environment2.getMatching(test2);
        List<Feature> result10 = environment2.getMatching(test3);
        List<Feature> result11 = environment2.getMatching(test4);
        List<Feature> result12 = environment2.getMatching(test5);
        List<Feature> result13 = environment2.getMatching(test6);
        List<Feature> result14 = environment2.getMatching(test7);

        assertEquals(3, result1.size());
        assertEquals(2, result2.size());
        assertEquals(1, result3.size());
        assertEquals(2, result4.size());
        assertEquals(1, result5.size());
        assertEquals(0, result6.size());
        assertEquals(0, result7.size());
        assertEquals(1, result8.size());
        assertEquals(0, result9.size());
        assertEquals(0, result10.size());
        assertEquals(0, result11.size());
        assertEquals(0, result12.size());
        assertEquals(1, result13.size());
        assertEquals(1, result14.size());

        assertEquals("java", result2.get(0).getName());
        assertEquals("1.8", result2.get(0).getVersion());
        assertEquals("java", result2.get(1).getName());
        assertEquals("1.7", result2.get(1).getVersion());

        assertEquals("java", result3.get(0).getName());
        assertEquals("1.8", result3.get(0).getVersion());

        assertEquals("java", result4.get(0).getName());
        assertEquals("1.8", result4.get(0).getVersion());
        assertEquals("java", result4.get(1).getName());
        assertEquals("1.7", result4.get(1).getVersion());

        assertEquals("java", result5.get(0).getName());
        assertEquals("1.8", result5.get(0).getVersion());

        assertEquals("python", result13.get(0).getName());
        assertEquals("3.7", result13.get(0).getVersion());

        assertEquals("python", result14.get(0).getName());
        assertEquals("3.7", result14.get(0).getVersion());
    }
}
