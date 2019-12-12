package at.enfilo.def.node.routine.factory.impl;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.common.util.environment.domain.Feature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Mockito.when;

public class CommandTemplateParserTest {

    private Environment environment;
    private Map<String, IPlaceholder> placeholderMap;

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
        String javaCmd = "java ({rbs}:-cp {}) test [test] [{cuda(>9)}:{$cudaJavaExtension}] {args} {pipes}";
        Feature java = new Feature("java", "1.8", "language", null, javaCmd, javaEnvironment, javaVariables);

        Map<String, String> cudaEnvironment = new HashMap<>();
        cudaEnvironment.put("PATH", "/{$path}/");
        Map<String, String> cudaVariables = new HashMap<>();
        cudaVariables.put("cudaJavaExtension", "-enableCuda [{framework}:{$fw}]");
        cudaVariables.put("path", "pathToCuda");
        Feature cuda = new Feature("cuda", "9.4", null, null, null, cudaEnvironment, cudaVariables);

        Map<String, String> frameworkEnvironment = new HashMap<>();
        frameworkEnvironment.put("var", "/{$path}/");
        Map<String, String> frameworkVariables = new HashMap<>();
        frameworkVariables.put("fw", "({rbs}:{$fwPath})");
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

        MatchingMode mode = MatchingMode.LOWEST;

        CommandTemplateParser parser1 = new CommandTemplateParser(environment, requiredFeatures1, placeholderMap, mode);
        CommandTemplateParser parser2 = new CommandTemplateParser(environment, requiredFeatures2, placeholderMap, mode);
        CommandTemplateParser parser3 = new CommandTemplateParser(environment, requiredFeatures3, placeholderMap, mode);
        CommandTemplateParser parser4 = new CommandTemplateParser(environment, requiredFeatures4, placeholderMap, mode);

        Map<String, List<String>> environment1 = parser1.parseEnvironmentVariables();
        Map<String, List<String>> environment2 = parser2.parseEnvironmentVariables();
        Map<String, List<String>> environment3 = parser3.parseEnvironmentVariables();
        Map<String, List<String>> environment4 = parser4.parseEnvironmentVariables();

        String cmd1 = parser1.parseCommand();
        String cmd2 = parser2.parseCommand();
        String cmd3 = parser3.parseCommand();
        String cmd4 = parser4.parseCommand();
        System.out.println("done");
    }

    /*private ITemplateDataProvider getDataProvider(IFeature feature) {
        if (feature instanceof Feature) {
            return new CommandTemplateParser.DataProvider() {
                @Override
                public String resolveVariable(String variable) {
                    if (feature.getVariables() != null && feature.getVariables().containsKey(variable)) {
                        return feature.getVariables().get(variable);
                    }
                    return null;
                }
            };
        } else if (feature instanceof Extension) {
            return new CommandTemplateParser.DataProvider() {
                @Override
                public String resolveVariable(String variable) {
                    if (feature.getVariables() != null && feature.getVariables().containsKey(variable)) {
                        return feature.getVariables().get(variable);
                    } else if (extensionFeatureMap.get(feature).getVariables() != null &&
                            extensionFeatureMap.get(feature).getVariables().containsKey(variable)) {
                        return extensionFeatureMap.get(feature).getVariables().get(variable);
                    }
                    return null;
                }
            };
        }
        return null;
    }

    private abstract class DataProvider implements ITemplateDataProvider {

        private String lastFeatureString;
        private String lastFeatureName;

        @Override
        public String resolvePlaceholder(String placeholder) {
            if (placeholder == null || placeholder.isEmpty()) {
                return null;
            }
            String key = placeholder.matches(".*\\d+") ? placeholder.replaceAll("\\d", "") + "*" : placeholder;
            return placeholderMap.containsKey(key) ? placeholderMap.get(key).get(placeholder) : null;
        }

        @Override
        public String resolveOptional(String reference, String template) {
            if (!requiresFeature(reference)) {
                return null;
            }
            Feature feature = features.get(lastFeatureName);
            List<Extension> extensionList = new ArrayList<>();
            for (String extensionName : Feature.getExtensionNames(lastFeatureString)) {
                extensionList.add(extensions.get(extensionName));
            }
            ITemplateDataProvider superProvider = this;
            return new Template(new CommandTemplateParser.DataProvider() {
                @Override
                public String resolveVariable(String variable) {
                    String result = superProvider.resolveVariable(variable);
                    if (result == null) {
                        if (feature.getVariables() != null && feature.getVariables().containsKey(variable)) {
                            return feature.getVariables().get(variable);
                        } else if (!extensionList.isEmpty()){
                            for (Extension extension : extensionList) {
                                if (extension.getVariables() != null && extension.getVariables().containsKey(variable)) {
                                    return extension.getVariables().get(variable);
                                }
                            }
                        }
                    }
                    return result;
                }
            }, template).parse();
        }

        private boolean requiresFeature(String featureString) {
            if (featureString == null || featureString.isEmpty()) {
                return false;
            }

            if (lastFeatureString.equals(featureString)) {
                return lastFeatureName != null;
            }
            lastFeatureString = featureString;
            if (requiredFeatures.matches(Collections.singletonList(featureString))) {
                lastFeatureName = Feature.getName(featureString);
                return true;
            } else {
                lastFeatureName = null;
                return false;
            }
        }

    }*/
}
