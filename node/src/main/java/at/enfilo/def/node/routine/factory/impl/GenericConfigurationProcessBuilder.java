package at.enfilo.def.node.routine.factory.impl;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.node.api.exception.RoutineCreationException;
import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.node.routine.factory.IRoutineProcessBuilderFactory;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericConfigurationProcessBuilder implements IRoutineProcessBuilderFactory {

    @Override
    public ProcessBuilder getRoutineProcessBuilder(RoutineDTO routine, SequenceStep sequenceStep, NodeConfiguration configuration, boolean addOutPipe) throws RoutineCreationException {
        Environment environment = configuration.getFeatureEnvironment();
        List<Feature> requiredFeatures = new ArrayList<>();
        for (FeatureDTO featureDTO : routine.getRequiredFeatures()) {
            Feature feature = new Feature(featureDTO.getName(), featureDTO.getVersion(), featureDTO.getGroup(), null);
            if (featureDTO.getExtensions() != null) {
                for (FeatureDTO extensionDTO : featureDTO.getExtensions()) {
                    Extension extension = new Extension(extensionDTO.getName(), extensionDTO.getVersion());
                    feature.addExtension(extension);
                }
            }
            requiredFeatures.add(feature);
        }
        Map<String, IPlaceholder> placeholderMap;
        try {
            placeholderMap = registerPlaceholders(new RoutineBinaryPlaceholder(routine.getRoutineBinaries()),
                    new ArgumentsPlaceholder(routine.getArguments()), new PipePlaceholder(sequenceStep, addOutPipe));
        } catch (MalformedURLException e) {
            throw new RoutineCreationException("Error creating routine", e);
        }

        CommandTemplateParser parser = new CommandTemplateParser(environment, requiredFeatures, placeholderMap, MatchingMode.LOWEST);
        ProcessBuilder processBuilder = new ProcessBuilder();
        buildEnvironment(processBuilder.environment(), parser);
        return processBuilder.command(parser.parseCommand().split(" "));
    }

    private void buildEnvironment(Map<String, String> processEnvironment, CommandTemplateParser parser) {
        Map<String, List<String>> environmentVariables = parser.parseEnvironmentVariables();
        if(environmentVariables == null || environmentVariables.isEmpty()) {
            return;
        }

        for (Map.Entry<String, List<String>> entry : environmentVariables.entrySet()) {
            if (processEnvironment.containsKey(entry.getKey())) {
                for (String part : entry.getValue()) {
                    processEnvironment.replace(entry.getKey(), processEnvironment.get(entry.getKey()) + ":" + part);
                }
            } else {
                processEnvironment.put(entry.getKey(), String.join(":", entry.getValue()));
            }
        }
    }

    private Map<String, IPlaceholder> registerPlaceholders(IPlaceholder... placeholders) {
        Map<String, IPlaceholder> placeholderMap = new HashMap<>();
        if (placeholders != null && placeholders.length > 0) {
            for (IPlaceholder placeholder : placeholders) {
                for (String key : placeholder.getKeywords()) {
                    placeholderMap.put(key, placeholder);
                }
            }
        }
        return placeholderMap;
    }
}
