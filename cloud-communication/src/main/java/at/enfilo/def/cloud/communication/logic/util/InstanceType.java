package at.enfilo.def.cloud.communication.logic.util;

import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;

/**
 * Enum for all different instance types the DEF offers
 */
public enum InstanceType {
    CLUSTER,
    WORKER,
    REDUCER;

    public static InstanceType getInstanceType(InstanceTypeDTO instanceTypeDTO) {
        switch (instanceTypeDTO) {
            case CLUSTER:
                return InstanceType.CLUSTER;
            case WORKER:
                return InstanceType.WORKER;
            case REDUCER:
                return InstanceType.REDUCER;
            default:
                return null;
        }
    }
}