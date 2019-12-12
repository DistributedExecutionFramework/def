package at.enfilo.def.node.routine.factory.impl;

import java.util.List;

public interface IPlaceholder {

    List<String> getKeywords();

    String get(String cmd);
}
