package at.enfilo.def.node.routine.factory.impl;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.common.util.environment.domain.Feature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class CommandTemplateParserTest {

    private Environment environment;
    private Environment environment2;
    private Map<String, IPlaceholder> placeholderMap;
    private Map<String, IPlaceholder> placeholderMap2;

    @Before
    public void setup() {
        Map<String, String> numpyEnvironment = new HashMap<>();
        numpyEnvironment.put("PATH", "/{$path}/");
        Map<String, String> numpyVariables = new HashMap<>();
        numpyVariables.put("numpyVar", "npy");
        numpyVariables.put("path", "pathToNpy");
        Extension numpy = new Extension("numpy", "1.15", null, numpyEnvironment, numpyVariables);

        Map<String, String> opencvVariables = new HashMap<>();
        opencvVariables.put("opencvVar", "ocv");
        opencvVariables.put("path", "pathToOcv");
        Extension opencv = new Extension("opencv", "1.9", null, null, opencvVariables);

        Extension matplotlib = new Extension("matplotlib", "1.5", null, null, null);

        Map<String, String> pythonEnvironment = new HashMap<>();
        pythonEnvironment.put("PATH", "/{$path}/");
        Map<String, String> pythonVariables = new HashMap<>();
        pythonVariables.put("pythonVar", "py");
        pythonVariables.put("path", "pathToPy");
        String pythonCmd = "python3 [{python(3.7):numpy(>1.15)}:{$path} {$numpyVar} {$pythonVar}] {rbs} {args} {pipes}";
        Feature python = new Feature("python", "3.7", "language", Arrays.asList(numpy, opencv, matplotlib), pythonCmd, pythonEnvironment, pythonVariables);

        Map<String, String> python2Environment = new HashMap<>();
        python2Environment.put("PATH", "/{$path}/");
        Map<String, String> python2Variables = new HashMap<>();
        python2Variables.put("pythonVar", "py");
        python2Variables.put("path", "pathToPy");
        String python2Cmd = "python [{python(2.7):numpy}:{$path} {$numpyVar} {$pythonVar}] {rbs} {args} {pipes}";
        Feature python2 = new Feature("python", "2.7", "language", Arrays.asList(numpy, opencv), python2Cmd, python2Environment, python2Variables);

        Map<String, String> javaEnvironment = new HashMap<>();
        javaEnvironment.put("JAVA_HOME", "/{$path}/");
        Map<String, String> javaVariables = new HashMap<>();
        javaVariables.put("javaVar", "jv");
        javaVariables.put("path", "pathToJv");
        String javaCmd = "java ({rbs}:-cp {} )test [test] [{cuda(>9)}:{$cudaJavaExtension}] {args} {pipes}";
        Feature java = new Feature("java", "1.8", "language", null, javaCmd, javaEnvironment, javaVariables);

        Map<String, String> cudaEnvironment = new HashMap<>();
        cudaEnvironment.put("PATH", "/{$path}/");
        Map<String, String> cudaVariables = new HashMap<>();
        cudaVariables.put("cudaJavaExtension", "-enableCuda [{framework}:{$fw}]");
        cudaVariables.put("path", "pathToCuda");
        Feature cuda = new Feature("cuda", "9.4", null, null, null, cudaEnvironment, cudaVariables);

        Map<String, String> frameworkEnvironment = new HashMap<>();
        frameworkEnvironment.put("var", "/{$fwPath}/");
        Map<String, String> frameworkVariables = new HashMap<>();
        frameworkVariables.put("fw", "({rbs}:{$fwPath}:)");
        frameworkVariables.put("fwPath", "pathToFramework");
        Feature framework = new Feature("framework", "9.4", null, null, null, frameworkEnvironment, frameworkVariables);

        environment = new Environment(Arrays.asList(python, python2, java, cuda, framework));
        placeholderMap = new HashMap<>();

        IPlaceholder rbPlaceholder = Mockito.mock(IPlaceholder.class);
        when(rbPlaceholder.get("rbs")).thenReturn("rb1 rb2");
        when(rbPlaceholder.get("rbp")).thenReturn("rbp");
        when(rbPlaceholder.get("rb0")).thenReturn("rb1");
        when(rbPlaceholder.get("rb1")).thenReturn("rb2");

        IPlaceholder aPlaceholder = Mockito.mock(IPlaceholder.class);
        when(aPlaceholder.get("args")).thenReturn("arg1 arg2");
        when(aPlaceholder.get("arg0")).thenReturn("arg1");
        when(aPlaceholder.get("arg1")).thenReturn("arg2");

        IPlaceholder pPlaceholder = Mockito.mock(IPlaceholder.class);
        when(pPlaceholder.get("pipes")).thenReturn("in ctrl");
        when(pPlaceholder.get("in")).thenReturn("in");
        when(pPlaceholder.get("out")).thenReturn(null);
        when(pPlaceholder.get("ctrl")).thenReturn("ctrl");

        placeholderMap.put("rbs", rbPlaceholder);
        placeholderMap.put("rbp", rbPlaceholder);
        placeholderMap.put("rb*", rbPlaceholder);
        placeholderMap.put("args", aPlaceholder);
        placeholderMap.put("arg*", aPlaceholder);
        placeholderMap.put("pipes", pPlaceholder);
        placeholderMap.put("in", pPlaceholder);
        placeholderMap.put("out", pPlaceholder);
        placeholderMap.put("ctrl", pPlaceholder);

        Map<String, String> dl4jVariables = new HashMap<>();
        dl4jVariables.put("dl4jPath", "/home/def/lib/dl4j/");
        Extension dl4j = new Extension("dl4j", "1.0.0.3", null, null, dl4jVariables);

        Map<String, String> nd4jVariables = new HashMap<>();
        nd4jVariables.put("nd4jPath", "/home/def/lib/nd4j/");
        Extension nd4j = new Extension("nd4j", "1.0.0.3", null, null, nd4jVariables);

        Map<String, String> javaVariables2 = new HashMap<>();
        javaVariables2.put("libraryPath", "/home/def/lib/");
        String javaCmd2 = "java -cp ({rbs}:{}:)[{java(1.8):dl4j}:{$dl4jPath}*:][{java(1.8):nd4j}:{$nd4jPath}*:] {arg0} {args} {pipes}";
        Feature java2 = new Feature("java", "1.8", "language", Arrays.asList(dl4j, nd4j), javaCmd2, null, javaVariables2);

        environment2 = new Environment(Collections.singletonList(java2));
        placeholderMap2 = new HashMap<>();

        IPlaceholder rbPlaceholder2 = Mockito.mock(IPlaceholder.class);
        when(rbPlaceholder2.get("rbs")).thenReturn("jar");
        when(rbPlaceholder2.get("rbp")).thenReturn("jar");
        when(rbPlaceholder2.get("rb0")).thenReturn("jar");

        IPlaceholder aPlaceholder2 = Mockito.mock(IPlaceholder.class);
        when(aPlaceholder2.get("args")).thenReturn("at.fhv.Class");
        when(aPlaceholder2.get("arg0")).thenReturn("at.fhv.Class");

        IPlaceholder pPlaceholder2 = Mockito.mock(IPlaceholder.class);
        when(pPlaceholder2.get("pipes")).thenReturn("in ctrl");
        when(pPlaceholder2.get("in")).thenReturn("in");
        when(pPlaceholder2.get("out")).thenReturn(null);
        when(pPlaceholder2.get("ctrl")).thenReturn("ctrl");

        placeholderMap2.put("rbs", rbPlaceholder2);
        placeholderMap2.put("rbp", rbPlaceholder2);
        placeholderMap2.put("rb*", rbPlaceholder2);
        placeholderMap2.put("args", aPlaceholder2);
        placeholderMap2.put("arg*", aPlaceholder2);
        placeholderMap2.put("pipes", pPlaceholder2);
        placeholderMap2.put("in", pPlaceholder2);
        placeholderMap2.put("out", pPlaceholder2);
        placeholderMap2.put("ctrl", pPlaceholder2);
    }

    @Test
    public void commandTemplateParser() {
        List<Feature> requiredFeatures1 = Collections.singletonList(
                Feature.buildFromString("java(>1.8)")
        );
        List<Feature> requiredFeatures2 = Collections.singletonList(
                Feature.buildFromString("python(>2.7):numpy")
        );
        List<Feature> requiredFeatures3 = Collections.singletonList(
                Feature.buildFromString("python(>2.7):numpy,matplotlib")
        );
        List<Feature> requiredFeatures4 = Arrays.asList(
                Feature.buildFromString("java(>1.8)"),
                Feature.buildFromString("cuda(>9)"),
                Feature.buildFromString("framework")
        );
        List<Feature> requiredFeatures5 = Collections.singletonList(
                Feature.buildFromString("java(>1.8):dl4j(1.0.0.3),nd4j(1.0.0.3)")
        );

        MatchingMode mode = MatchingMode.LOWEST;
        CommandTemplateParser parser1 = new CommandTemplateParser(environment, requiredFeatures1, placeholderMap, mode);
        CommandTemplateParser parser2 = new CommandTemplateParser(environment, requiredFeatures2, placeholderMap, mode);
        CommandTemplateParser parser3 = new CommandTemplateParser(environment, requiredFeatures3, placeholderMap, mode);
        CommandTemplateParser parser4 = new CommandTemplateParser(environment, requiredFeatures4, placeholderMap, mode);
        CommandTemplateParser parser5 = new CommandTemplateParser(environment2, requiredFeatures5, placeholderMap2, mode);

        Map<String, List<String>> environment1 = parser1.parseEnvironmentVariables();
        Map<String, List<String>> environment2 = parser2.parseEnvironmentVariables();
        Map<String, List<String>> environment3 = parser3.parseEnvironmentVariables();
        Map<String, List<String>> environment4 = parser4.parseEnvironmentVariables();
        Map<String, List<String>> environment5 = parser5.parseEnvironmentVariables();
        String cmd1 = parser1.parseCommand();
        String cmd2 = parser2.parseCommand();
        String cmd3 = parser3.parseCommand();
        String cmd4 = parser4.parseCommand();
        String cmd5 = parser5.parseCommand();

        assertEquals(1, environment1.size());
        assertEquals(1, environment2.size());
        assertEquals(1, environment3.size());
        assertEquals(3, environment4.size());
        assertEquals(0, environment5.size());

        assertTrue(environment1.containsKey("JAVA_HOME"));
        assertTrue(environment2.containsKey("PATH"));
        assertTrue(environment3.containsKey("PATH"));
        assertTrue(environment4.containsKey("JAVA_HOME"));
        assertTrue(environment4.containsKey("PATH"));
        assertTrue(environment4.containsKey("var"));

        assertEquals(1, environment1.get("JAVA_HOME").size());
        assertEquals(2, environment2.get("PATH").size());
        assertEquals(2, environment3.get("PATH").size());
        assertEquals(1, environment4.get("PATH").size());
        assertEquals(1, environment4.get("var").size());
        assertEquals(1, environment4.get("JAVA_HOME").size());

        assertTrue(environment1.get("JAVA_HOME").contains("/pathToJv/"));
        assertTrue(environment2.get("PATH").contains("/pathToNpy/"));
        assertTrue(environment2.get("PATH").contains("/pathToPy/"));
        assertTrue(environment3.get("PATH").contains("/pathToNpy/"));
        assertTrue(environment3.get("PATH").contains("/pathToPy/"));
        assertTrue(environment4.get("JAVA_HOME").contains("/pathToJv/"));
        assertTrue(environment4.get("var").contains("/pathToFramework/"));
        assertTrue(environment4.get("PATH").contains("/pathToCuda/"));

        assertEquals("java -cp rb1 -cp rb2 test [test] arg1 arg2 in ctrl", cmd1);
        assertEquals("python pathToPy npy py rb1 rb2 arg1 arg2 in ctrl", cmd2);
        assertEquals("python3 pathToPy npy py rb1 rb2 arg1 arg2 in ctrl", cmd3);
        assertEquals("java -cp rb1 -cp rb2 test [test] -enableCuda pathToFramework:pathToFramework: arg1 arg2 in ctrl", cmd4);
        assertEquals("java -cp jar:/home/def/lib/dl4j/*:/home/def/lib/nd4j/*: at.fhv.Class at.fhv.Class in ctrl", cmd5);
    }
}
