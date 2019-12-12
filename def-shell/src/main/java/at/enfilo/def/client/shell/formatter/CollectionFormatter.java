package at.enfilo.def.client.shell.formatter;

import java.util.Collection;
import java.util.stream.Collectors;

class CollectionFormatter extends ShellFormatter<Collection> {

	public CollectionFormatter() {
		super(Collection.class);
	}

	@Override
	public String doFormat(Collection objects, char[] shifted) {
	    StringBuilder sb = new StringBuilder();

	    String formattedCollection = ((Collection<?>) objects).stream().map(ShellOutputFormatter::format).collect(
            Collectors.joining("\n", "\t", "")
        );

	    sb.append(shifted).append("[").append("\n");
	    sb.append(shifted).append(formattedCollection).append("\n");
	    sb.append(shifted).append("]").append("\n");

	    return sb.toString();
	}
}
