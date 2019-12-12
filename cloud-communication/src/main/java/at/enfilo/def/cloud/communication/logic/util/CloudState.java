package at.enfilo.def.cloud.communication.logic.util;

import at.enfilo.def.cloud.communication.logic.general.CloudInstance;

/**
 * Enum with all different states a {@link CloudInstance} can have
 */
public enum CloudState {
    CREATED,
    BOOTING,
    RUNNING,
    SHUTTING_DOWN,
    TERMINATED,
    STOPPING,
    STOPPED,
    UNDEFINED,
    ERROR
}