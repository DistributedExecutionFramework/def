package at.enfilo.def.common.util.environment.domain;

import java.util.*;

public class Environment {

    private List<Feature> features = new ArrayList<>();
    private Map<String, List<Integer>> featureMap = new HashMap<>();

    public Environment() { }

    public Environment(List<Feature> features) {
        if (features != null && !features.isEmpty()) {
            this.features = features;
            for (int i = 0; i < features.size(); i++) {
                Feature current = features.get(i);
                if (featureMap.containsKey(current.getName())) {
                    featureMap.get(current.getName()).add(i);
                } else {
                    List<Integer> indices = new ArrayList<>();
                    indices.add(i);
                    featureMap.put(current.getName(), indices);
                }
            }
        }
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    Map<String, List<Integer>> getFeatureMap() {
        return new HashMap<>(featureMap);
    }

    public void addFeature(Feature feature) {
        if (features == null) {
            features = new ArrayList<>();
        }
        features.add(feature);
        if (featureMap.containsKey(feature.getName())) {
            featureMap.get(feature.getName()).add(features.size() - 1);
        } else {
            List<Integer> indices = new ArrayList<>();
            indices.add(features.size() - 1);
            featureMap.put(feature.getName(), indices);
        }
    }

    public List<String> getAsString() {
        List<String> environment = new ArrayList<>();
        for (Feature feature : features) {
            environment.add(feature.toString());
        }
        return environment;
    }

    public static Environment buildFromString(List<String> environment) {
        if (environment == null || environment.isEmpty()) {
            return new Environment();
        }
        List<Feature> features = new ArrayList<>();
        for (String entry : environment) {
            Feature feature = Feature.buildFromString(entry);
            if (feature != null) {
                features.add(feature);
            }
        }
        return new Environment(features);
    }

    public boolean matches(List<Feature> required) {
        if (required == null || required.isEmpty()) {
            return true;
        }

        for (Feature entry : required) {
            if (!matches(entry)) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(Environment required) {
        if (required == null || required.getFeatures() == null || required.getFeatures().isEmpty()) {
            return true;
        }

        for (Feature feature : required.getFeatures()) {
            if (!matches(feature)) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(Feature feature) {
        return feature == null || feature.getName() == null || feature.getName().isEmpty()
                || featureMap.containsKey(feature.getName()) && matches(featureMap.get(feature.getName()), feature);
    }

    private boolean matches(List<Integer> indices, Feature feature) {
        for (Integer i : indices) {
            if (features.get(i).matches(feature)) {
                return true;
            }
        }
        return false;
    }

    public List<Feature> getMatching(Feature feature) {
        if (features == null || features.isEmpty()) {
            return new ArrayList<>();
        }
        if(feature == null || feature.getName() == null || feature.getName().isEmpty()) {
            return features;
        }

        if (!featureMap.containsKey(feature.getName())) {
            return new ArrayList<>();
        }
        List<Integer> indices = featureMap.get(feature.getName());
        List<Feature> matching = new ArrayList<>();
        for (Integer i : indices) {
            if (features.get(i).matches(feature)) {
                matching.add(features.get(i));
            }
        }
        return matching;
    }
}
