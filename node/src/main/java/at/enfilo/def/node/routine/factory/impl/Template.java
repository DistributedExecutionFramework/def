package at.enfilo.def.node.routine.factory.impl;

import java.util.ArrayList;
import java.util.List;

import static at.enfilo.def.common.util.environment.EnvironmentPatterns.COMMAND_TEMPLATE_DELIMITERS;
import static at.enfilo.def.common.util.environment.EnvironmentPatterns.TemplatePart;

public class Template {

    private List<String> template;
    private ITemplateDataProvider dataProvider;

    private boolean parsed = false;

    Template(ITemplateDataProvider dataProvider, String in) {
        this.dataProvider = dataProvider;
        this.template = split(in);
    }

    List<String> getTemplate() {
        return template;
    }

    public String parse() {
        while (!parsed) {
            parsed = true;
            for (int i = 0; i < template.size(); i++) {
                String current = template.get(i);
                switch (TemplatePart.match(current)) {
                    case PLACEHOLDER:
                        String placeholder = current.substring(1, current.length() - 1);
                        String placeholderResult = dataProvider.resolvePlaceholder(placeholder);
                        if (placeholderResult != null) {
                            template.set(i, placeholderResult);
                        } else {
                            template.remove(i);
                            checkDoubleWhitespace(i);
                        }
                        parsed = false;
                        break;
                    case VARIABLE:
                        String variable = current.substring(2, current.length() - 1);
                        String variableResult = dataProvider.resolveVariable(variable);
                        if (variableResult != null) {
                            List<String> varResult = split(variableResult);
                            template.set(i, varResult.get(0));
                            template.addAll(i + 1, varResult.subList(1, varResult.size()));
                        } else {
                            template.remove(i);
                            checkDoubleWhitespace(i);
                        }
                        parsed = false;
                        break;
                    case LOOP:
                        String[] loopSplit = split(current, ':');
                        String pH = dataProvider.resolvePlaceholder(loopSplit[0]);
                        if (pH != null && !pH.isEmpty()) {
                            List<String> loopResult = new ArrayList<>();
                            for (String curVar : pH.split(" ")) {
                                loopResult.addAll(split(loopSplit[1].replace("{}", curVar)));
                            }
                            template.set(i, loopResult.get(0));
                            template.addAll(i + 1, loopResult.subList(1, loopResult.size()));
                        } else {
                            template.remove(i);
                            checkDoubleWhitespace(i);
                        }
                        parsed = false;
                        break;
                    case OPTIONAL:
                        String[] optionalSplit = split(current, ':');
                        String optionalTemplate = dataProvider.resolveOptional(optionalSplit[0], optionalSplit[1]);
                        if (optionalTemplate != null) {
                            template.set(i, optionalTemplate);
                        } else {
                            template.remove(i);
                            checkDoubleWhitespace(i);
                        }
                        parsed = false;
                        break;
                }
            }
        }
        return String.join("", template);
    }

    private void checkDoubleWhitespace(int idx) {
        if (idx > 0) {
            if (idx == template.size()) {
                if (template.get(idx - 1).equals(" ")) {
                    template.remove(idx - 1);
                }
            } else if (template.get(idx).equals(" ") && template.get(idx - 1).equals(" ")) {
                template.remove(idx);
            }
        } else {
            if (idx < template.size() && template.get(idx).equals(" ")) {
                template.remove(idx);
            }
        }
    }

    private String[] split(String string, char pattern) {
        string = string.substring(1, string.length() - 1);
        String[] split = new String[2];
        int splitIdx = 0;
        int counter = 0;
        while (string.charAt(splitIdx) != pattern) {
            char curChar = string.charAt(splitIdx);
            if (COMMAND_TEMPLATE_DELIMITERS.containsKey(curChar)) {
                counter++;
                splitIdx++;

                do {
                    if (string.charAt(splitIdx) == curChar) {
                        counter++;
                    } else if (string.charAt(splitIdx) == COMMAND_TEMPLATE_DELIMITERS.get(curChar)) {
                        counter--;
                    }
                    splitIdx++;
                } while (counter > 0 && splitIdx < string.length());
                continue;
            }
            splitIdx++;
        }
        split[0] = string.substring(1, splitIdx - 1);
        split[1] = string.substring(splitIdx + 1);
        return split;
    }

    private List<String> split(String in) {
        List<String> template = new ArrayList<>();
        int i = 0;
        while (in != null && !in.isEmpty()) {
            if (i == in.length()) {
                template.add(in);
                in = null;
                continue;
            }
            if (COMMAND_TEMPLATE_DELIMITERS.containsKey(in.charAt(i))) {
                if (i > 0) {
                    template.add(in.substring(0, i));
                    in = in.substring(i);
                    i = 0;
                }

                char delimiter = in.charAt(i);
                int counter = 0;

                do {
                    if (in.charAt(i) == delimiter) {
                        counter++;
                    }
                    if (in.charAt(i) == COMMAND_TEMPLATE_DELIMITERS.get(delimiter)) {
                        counter--;
                    }
                    i++;
                } while (counter > 0 && i < in.length());

                int end = i > in.length() ? in.length() : i;
                template.add(in.substring(0, end));
                in = in.substring(end);
                i = 0;
                continue;
            }
            i++;
        }
        return template;
    }
}
