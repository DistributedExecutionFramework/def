package at.enfilo.def.node.routine.factory.impl;

public interface ITemplateDataProvider {

    String resolvePlaceholder(String placeholder);

    String resolveVariable(String variable);

    String resolveOptional(String reference, String template);
}
