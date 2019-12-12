package at.enfilo.def.node.routine.factory.impl;

import at.enfilo.def.common.util.CollectionUtils;
import at.enfilo.def.common.util.environment.VersionMatcher;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.common.util.environment.domain.IFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandTemplateParser {

    private Map<String, Feature> features = new HashMap<>();
    private Map<String, Extension> extensions = new HashMap<>();
    private Map<Extension, Feature> extensionFeatureMap = new HashMap<>();
    private Map<String, IPlaceholder> placeholderMap;

    private Feature languageFeature;

    CommandTemplateParser(Environment environment, List<Feature> requiredFeatures, Map<String, IPlaceholder> placeholderMap, MatchingMode mode) {
        this.placeholderMap = placeholderMap;
        if (requiredFeatures != null && !requiredFeatures.isEmpty()) {
            for (Feature feature : requiredFeatures) {
                List<Feature> matchingFeatures = environment.getMatching(feature);
                Feature selected = reduceResultSet(matchingFeatures, mode);
                if (selected == null) {
                    continue;
                }
                features.put(selected.getName(), selected);
                if (selected.getGroup() != null && selected.getGroup().equalsIgnoreCase("language")) {
                    languageFeature = selected;
                }

                if (feature.getExtensions() != null && !feature.getExtensions().isEmpty()) {
                    for (Extension extension : feature.getExtensions()) {
                        List<Extension> matchingExtensions = selected.getMatching(extension);
                        Extension selectedEx = reduceResultSet(matchingExtensions, mode);
                        if (selectedEx == null) {
                            continue;
                        }
                        extensions.put(selectedEx.getName(), selectedEx);
                        extensionFeatureMap.put(selectedEx, selected);
                    }
                }
            }
        }
    }

    public Map<String, List<String>> parseEnvironmentVariables() throws IllegalArgumentException {
        Map<String, List<String>> environmentVariables = new HashMap<>();

        List<IFeature> entries = new ArrayList<>(extensions.values());
        entries.addAll(features.values());

        for (IFeature current : entries) {
            if (current.getEnvironment() != null && !current.getEnvironment().isEmpty()) {
                for (Map.Entry<String, String> entry : current.getEnvironment().entrySet()) {
                    CollectionUtils.putOrAdd(environmentVariables, entry.getKey(),
                            new Template(getDataProvider(current), entry.getValue()).parse());
                }
            }
        }

        return environmentVariables;
    }

    public String parseCommand() throws IllegalArgumentException {
        if (languageFeature != null) {
            return new Template(getDataProvider(languageFeature), languageFeature.getCmd()).parse();
        } else {
            throw new IllegalArgumentException("No feature of group \"language\" specified");
        }
    }

    private <T extends IFeature> T reduceResultSet(List<T> features, MatchingMode mode) {
        if (features == null || features.isEmpty()) {
            return null;
        }
        T selected = features.get(0);
        if (features.size() > 1) {
            for (T match : features.subList(1, features.size())) {
                if (mode == MatchingMode.LOWEST && !VersionMatcher.matchLower(selected.getVersion(), match.getVersion())) {
                    selected = match;
                } else if (mode == MatchingMode.HIGHEST
                        && !VersionMatcher.matchHigher(selected.getVersion(), match.getVersion())) {
                    selected = match;
                }
            }
        }
        return selected;
    }

    ITemplateDataProvider getDataProvider(IFeature feature) {
        if (feature instanceof Feature) {
            return new DataProvider() {
                @Override
                public String resolveVariable(String variable) {
                    if (feature.getVariables() != null && feature.getVariables().containsKey(variable)) {
                        return feature.getVariables().get(variable);
                    }
                    return null;
                }
            };
        } else if (feature instanceof Extension) {
            return new DataProvider() {
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
            Feature feature = features.get(Feature.getName(reference));
            List<Extension> extensionList = new ArrayList<>();
            List<String> extensionNames = Feature.getExtensionNames(reference);
            if (extensionNames != null) {
                for (String extensionName : Feature.getExtensionNames(reference)) {
                    extensionList.add(extensions.get(extensionName));
                }
            }
            ITemplateDataProvider superProvider = this;
            return new Template(new DataProvider() {
                @Override
                public String resolveVariable(String variable) {
                    String result = superProvider.resolveVariable(variable);
                    if (result == null) {
                        if (feature.getVariables() != null && feature.getVariables().containsKey(variable)) {
                            return feature.getVariables().get(variable);
                        } else if (!extensionList.isEmpty()) {
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

            Feature optionalFeature = Feature.buildFromString(featureString);
            if (optionalFeature.getExtensions() != null) {
                for (Extension extension : optionalFeature.getExtensions()) {
                    if (!extensions.containsKey(extension.getName())
                            || !extensions.get(extension.getName()).matches(extension)) {
                        return false;
                    }
                }
            }
            optionalFeature.getExtensions().clear();
            return features.containsKey(optionalFeature.getName())
                    && features.get(optionalFeature.getName()).matches(optionalFeature);
        }

    }
}