package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.domain.entity.Tag;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.TagDTO;
import at.enfilo.def.transfer.util.MapManager;

import java.util.stream.Collectors;

/**
 * Created by aer on 22.01.2018.
 */
public class FeatureDTOMapper extends AbstractMapper<Feature, FeatureDTO> {

    public FeatureDTOMapper() {
        super(Feature.class, FeatureDTO.class);
    }

    @Override
    public FeatureDTO map(Feature source, FeatureDTO destination)
            throws IllegalArgumentException, IllegalStateException {
        FeatureDTO dest = destination;
        if (dest == null) {
            dest = new FeatureDTO();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getGroup, dest::setGroup);
            mapAttributes(source::getName, dest::setName);
            mapAttributes(source::getVersion, dest::setVersion);
            mapAttributes(source::getBaseFeature, dest::setBaseId, Feature::getId);
            mapAttributes(source::getSubFeatures, dest::setExtensions,
                    features -> MapManager.map(features, FeatureDTO.class).collect(Collectors.toList()));

            return dest;
        }
        return null;
    }
}
