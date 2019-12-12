package at.enfilo.def.parameterserver.impl.store;

import at.enfilo.def.transfer.dto.ParameterType;

public interface IStoreDriver {

    boolean exists(String programId, String parameterId);

    Object read(String programId, String parameterId);

    String readTypeId(String programId, String parameterId);

    String store(String programId, String parameterId, Object data, String typeId);

    String store(String programId, String parameterId, Object data, String typeId, ParameterType type);

    String update(String programId, String parameterId, Object data);

    boolean delete(String programId, String parameterId);

    boolean deleteAll(String programId);
}
