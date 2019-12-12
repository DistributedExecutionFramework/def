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
        template = split(in);
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
                        String placeholder = dataProvider.resolvePlaceholder(current.substring(1, current.length() - 1));
                        if (placeholder != null) {
                            template.set(i, placeholder);
                        } else {
                            template.remove(i);
                            checkDoubleWhitespace(i);
                        }
                        parsed = false;
                        break;
                    case VARIABLE:
                        String variable = dataProvider.resolveVariable(current.substring(2, current.length() - 1));
                        if (variable != null) {
                            List<String> varResult = split(variable);
                            template.set(i, varResult.get(0));
                            template.addAll(i + 1, varResult.subList(1, varResult.size()));
                        } else {
                            template.remove(i);
                            checkDoubleWhitespace(i);
                        }
                        parsed = false;
                        break;
                    case LOOP:
                        String[] loopSplit = current.split(":");
                        String loopVar = loopSplit[0].substring(2, loopSplit[0].length() - 1);
                        String loopBody = loopSplit[1].substring(0, loopSplit[1].length() - 1);
                        String pH = dataProvider.resolvePlaceholder(loopVar);
                        if (pH != null && !pH.isEmpty()) {
                            List<String> loopResult = new ArrayList<>();
                            for (String curVar : pH.split(" ")) {
                                loopResult.addAll(split(loopBody.replace("{}", curVar)));
                                loopResult.add(" ");
                            }
                            loopResult.remove(loopResult.size() - 1);
                            template.set(i, loopResult.get(0));
                            template.addAll(i + 1, loopResult.subList(1, loopResult.size()));
                        } else {
                            template.remove(i);
                            checkDoubleWhitespace(i);
                        }
                        parsed = false;
                        break;
                    case OPTIONAL:
                        String[] optionalSplit = current.length() - current.replace(":", "").length() == 1 ?
                                current.split(":") : split(current, ":", 1);

                        String optionalVar = optionalSplit[0].substring(2, optionalSplit[0].length() - 1);
                        String optionalBody = optionalSplit[1].substring(0, optionalSplit[1].length() - 1);
                        String optionalTemplate = dataProvider.resolveOptional(optionalVar, optionalBody);
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

    private String[] split(String string, String pattern, int ignoreFirstN) {
        String[] split = string.split(pattern);
        if (ignoreFirstN < split.length) {
            String[] result = new String[split.length - ignoreFirstN];
            StringBuilder first = new StringBuilder(split[0]);
            int i;
            for (i = 1; i <= ignoreFirstN; i++) {
                first.append(pattern).append(split[i]);
            }
            result[0] = first.toString();
            for (int j = 1; j < result.length; j++, i++) {
                result[j] = split[i];
            }
            return result;

        } else {
            return new String[]{string};
        }
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
                    in = in.substring(i, in.length());
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
                in = in.substring(end, in.length());
                i = 0;
            }
            i++;
        }
        return template;
    }
}
