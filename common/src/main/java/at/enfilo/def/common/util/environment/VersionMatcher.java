package at.enfilo.def.common.util.environment;

import static at.enfilo.def.common.util.environment.EnvironmentPatterns.*;

public abstract class VersionMatcher {

    public static boolean matchVersion(String environmentVersion, String requiredVersion) {
        if (requiredVersion == null || requiredVersion.isEmpty()) {
            return true;
        }
        if (environmentVersion == null || environmentVersion.isEmpty()) {
            return false;
        }

        String[] requiredVersions = requiredVersion.split(",");
        boolean match = false;

        for (String option : requiredVersions) {
            if (option.matches(VERSION_PATTERN_EXACT)) {
                match = matchExact(environmentVersion, option);
            } else if (option.matches(VERSION_PATTERN_HIGHER)) {
                match = matchHigher(environmentVersion, option.substring(1));
            } else if (option.matches(VERSION_PATTERN_LOWER)) {
                match = matchLower(environmentVersion, option.substring(1));
            } else if (option.matches(VERSION_PATTERN_INTERVAL)) {
                match = matchInterval(environmentVersion, option);
            }

            if(match) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchExact(String eVersion, String rVersion) {
        String[] eVersionSplit = eVersion.split("\\.");
        String[] rVersionSplit = rVersion.split("\\.");
        if (rVersionSplit.length > eVersionSplit.length) {
            return false;
        }
        for (int i = 0; i < rVersionSplit.length; i++) {
            if (!rVersionSplit[i].equals(eVersionSplit[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchHigher(String eVersion, String rVersion) {
        String[] eVersionSplit = eVersion.split("\\.");
        String[] rVersionSplit = rVersion.split("\\.");
        for (int i = 0; i < rVersionSplit.length; i++) {
            if (i >= eVersionSplit.length) {
                return false;
            }
            if (Integer.valueOf(eVersionSplit[i]) > Integer.valueOf(rVersionSplit[i])) {
                return true;
            }
            if (Integer.valueOf(eVersionSplit[i]) < Integer.valueOf(rVersionSplit[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchLower(String eVersion, String rVersion) {
        String[] eVersionSplit = eVersion.split("\\.");
        String[] rVersionSplit = rVersion.split("\\.");
        for (int i = 0; i < rVersionSplit.length; i++) {
            if (i >= eVersionSplit.length) {
                return true;
            }
            if (Integer.valueOf(eVersionSplit[i]) < Integer.valueOf(rVersionSplit[i])) {
                return true;
            }
            if (Integer.valueOf(eVersionSplit[i]) > Integer.valueOf(rVersionSplit[i])) {
                return false;
            }
        }
        return eVersionSplit.length <= rVersionSplit.length;
    }

    public static boolean matchInterval(String eVersion, String rVersion) {
        String[] interval = rVersion.split("-");
        return matchHigher(eVersion, interval[0]) && matchLower(eVersion, interval[1]);
    }
}

