package at.enfilo.def.transfer.util;

import at.enfilo.def.transfer.dto.ResourceDTO;

public class ResourceUtil {
	public static boolean isSharedResource(ResourceDTO resource) {
		return (resource.isSetId()
				&& !resource.getId().isEmpty()
				&& (resource.getDataTypeId() == null || resource.getDataTypeId().isEmpty())
				&& (resource.data == null || !resource.data.hasArray())
				&& (resource.getUrl() == null || resource.getUrl().isEmpty())
		);
	}
}
