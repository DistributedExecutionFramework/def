package at.enfilo.def.transfer.util.mappers.impl;


import at.enfilo.def.domain.entity.Tag;
import at.enfilo.def.transfer.dto.TagDTO;

/**
 * Created by mase on 16.06.2016.
 */
public class TagMapper extends AbstractMapper<TagDTO, Tag> {

    public TagMapper() {
        super(TagDTO.class, Tag.class);
    }

    @Override
    public Tag map(TagDTO source, Tag destination)
    throws IllegalArgumentException, IllegalStateException {


        Tag dest = destination;
        if (dest == null) {
            dest = new Tag();
        }

        if (source != null) {
            mapAttributes(source::getId, dest::setId);
            mapAttributes(source::getDescription, dest::setDescription);

            return dest;
        }
        return null;
    }
}
