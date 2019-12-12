package at.enfilo.def.common.util.environment;

import java.util.HashMap;
import java.util.Map;

public abstract class EnvironmentPatterns {

    public static final Map<Character, Character> COMMAND_TEMPLATE_DELIMITERS;

    static {
        Map<Character, Character> map = new HashMap<>();
        map.put('{', '}');
        map.put('(', ')');
        map.put('[', ']');
        COMMAND_TEMPLATE_DELIMITERS = map;
    }

    public static final String VERSION_PATTERN_EXACT = "[0-9]+(\\.[0-9]+)*";
    public static final String VERSION_PATTERN_HIGHER = ">" + VERSION_PATTERN_EXACT;
    public static final String VERSION_PATTERN_LOWER = "<" + VERSION_PATTERN_EXACT;
    public static final String VERSION_PATTERN_INTERVAL = VERSION_PATTERN_EXACT + "-" + VERSION_PATTERN_EXACT;
    public static final String VERSION_PATTERN_SINGLE = "(" + VERSION_PATTERN_EXACT + "|" + VERSION_PATTERN_HIGHER + "|" + VERSION_PATTERN_LOWER + "|" + VERSION_PATTERN_INTERVAL + ")";
    public static final String VERSION_PATTERN = VERSION_PATTERN_SINGLE + "(," + VERSION_PATTERN_SINGLE + ")*";

    public static final String FEATURE_NAME_PATTERN = "[a-zA-Z0-9_\\-]+";
    public static final String FEATURE_PATTERN_SINGLE = FEATURE_NAME_PATTERN + "(\\(" + VERSION_PATTERN + "\\))?";
    public static final String FEATURE_PATTERN = FEATURE_PATTERN_SINGLE + "(:" + FEATURE_PATTERN_SINGLE + "(," + FEATURE_PATTERN_SINGLE + ")*)?";

    public static final String COMMAND_TEMPLATE_PLACEHOLDER_VARIABLE_NAME_PATTERN = "[a-zA-Z0-9_\\-]+";
    public static final String COMMAND_TEMPLATE_PLACEHOLDER = "\\{" + COMMAND_TEMPLATE_PLACEHOLDER_VARIABLE_NAME_PATTERN + "}";
    public static final String COMMAND_TEMPLATE_VARIABLE = "\\{\\$" + COMMAND_TEMPLATE_PLACEHOLDER_VARIABLE_NAME_PATTERN + "}";
    public static final String COMMAND_TEMPLATE_LOOP = "\\(" + COMMAND_TEMPLATE_PLACEHOLDER + ":.+\\)";
    public static final String COMMAND_TEMPLATE_OPTIONAL = "\\[\\{" + FEATURE_PATTERN + "}:.+]";

    public enum TemplatePart {
        LITERAL, PLACEHOLDER, VARIABLE, LOOP, OPTIONAL;

        public static TemplatePart match(String templatePart) {
            if (templatePart.matches(COMMAND_TEMPLATE_VARIABLE)) {
                return TemplatePart.VARIABLE;
            } else if (templatePart.matches(COMMAND_TEMPLATE_PLACEHOLDER)) {
                return TemplatePart.PLACEHOLDER;
            } else if (templatePart.matches(COMMAND_TEMPLATE_LOOP)) {
                return TemplatePart.LOOP;
            } else if (templatePart.matches(COMMAND_TEMPLATE_OPTIONAL)) {
                return TemplatePart.OPTIONAL;
            } else {
                return TemplatePart.LITERAL;
            }
        }
    }
}
