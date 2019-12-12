package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.domain.entity.Tag;
import at.enfilo.def.transfer.dto.TagDTO;

/**
 * Created by mase on 16.06.2016.
 */
public class TagDTOMapper extends AbstractMapper<Tag, TagDTO> {

    public TagDTOMapper() {
        super(Tag.class, TagDTO.class);
    }

    @Override
    public TagDTO map(Tag source, TagDTO destination)
    throws IllegalArgumentException, IllegalStateException {

        TagDTO dest = destination;
        if (dest == null) {
            dest = new TagDTO();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getDescription, dest::setDescription);

            return dest;
        }
        return null;
    }
}
