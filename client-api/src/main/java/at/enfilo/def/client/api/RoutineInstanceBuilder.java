package at.enfilo.def.client.api;

import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.RoutineInstanceDTO;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.TSerializer;

import java.util.HashMap;
import java.util.Map;

public class RoutineInstanceBuilder {
	private static final TSerializer SERIALIZER = new TSerializer();

	private RoutineInstanceDTO routineInstance;
	private Map<String, ResourceDTO> inParams;

	public RoutineInstanceBuilder(String routineId) {
		routineInstance = new RoutineInstanceDTO();
		routineInstance.setRoutineId(routineId);
		inParams = new HashMap<>();
	}

	public RoutineInstanceBuilder addParameter(String name, TBase value) throws TException {
		ResourceDTO resource = new ResourceDTO();
		TFieldIdEnum idField = value.fieldForId((short)1); // Convention: field with id 1 is '_id'
		String dataTypeId = value.getFieldValue(idField).toString();
		resource.setDataTypeId(dataTypeId);
		resource.setData(SERIALIZER.serialize(value));
		inParams.put(name, resource);
		return this;
	}

	public RoutineInstanceBuilder addParameter(String name, String shareResourceId) {
		ResourceDTO sharedResource = new ResourceDTO();
		sharedResource.setId(shareResourceId);
		inParams.put(name, sharedResource);
		return this;
	}

	public RoutineInstanceDTO build() {
		routineInstance.setInParameters(inParams);
		return routineInstance;
	}
}
