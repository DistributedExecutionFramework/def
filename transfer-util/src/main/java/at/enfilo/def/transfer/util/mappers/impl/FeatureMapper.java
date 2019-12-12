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
public class FeatureMapper extends AbstractMapper<FeatureDTO, Feature> {

    public FeatureMapper() {
        super(FeatureDTO.class, Feature.class);
    }

    @Override
    public Feature map(FeatureDTO source, Feature destination)
            throws IllegalArgumentException, IllegalStateException {
        Feature dest = destination;
        if (dest == null) {
            dest = new Feature();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getGroup, dest::setGroup);
            mapAttributes(source::getName, dest::setName);
            mapAttributes(source::getVersion, dest::setVersion);
            //mapAttributes(source::getBaseId, dest::setBaseFeature, Feature::getId);
            mapAttributes(source::getExtensions, dest::setSubFeatures,
                    features -> MapManager.map(features, Feature.class).collect(Collectors.toList()));

            return dest;
        }
        return null;
    }
}
