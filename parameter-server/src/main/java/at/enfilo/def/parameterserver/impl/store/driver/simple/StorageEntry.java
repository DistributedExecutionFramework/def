package at.enfilo.def.parameterserver.impl.store.driver.simple;

import at.enfilo.def.transfer.dto.ParameterType;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StorageEntry {

    private Object data;
    private String typeId;
    private ParameterType type = ParameterType.READ_WRITE;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public StorageEntry(Object data, String typeId) {
        this.data = data;
        this.typeId = typeId;
    }

    public StorageEntry(Object data, String typeId, ParameterType type) {
        this.data = data;
        this.typeId = typeId;
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public ParameterType getType() {
        return type;
    }
}
