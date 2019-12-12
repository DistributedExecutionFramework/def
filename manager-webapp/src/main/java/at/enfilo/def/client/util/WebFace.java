package at.enfilo.def.client.util;

/**
 * Created by mase on 22.08.2016.
 */
public enum WebFace {

    AUTH_FACE("/auth"),
    RESTRICTED_AREA("/control"),
    PROGRAMS_FACE(RESTRICTED_AREA + "/programs"),
    JOBS_FACE(RESTRICTED_AREA + "/jobs"),
    TASKS_FACE(RESTRICTED_AREA + "/tasks");

    private static final String FACE_REFERENCE_FORMAT = "%s%s";
    private static final String SUFFIX = ".jsf";

    private final String facePath;

    WebFace(String facePath) {
        this.facePath = facePath;
    }

    public String getReference() {
        return String.format(FACE_REFERENCE_FORMAT, facePath, SUFFIX);
    }

    public String getRawPath() {
        return facePath;
    }

    @Override
    public String toString() {
        return getRawPath();
    }
}
