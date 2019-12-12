package at.enfilo.def.client;

import org.springframework.shell.Bootstrap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Start DEF Shell
 */
public class DEFShell {

	public static void main(String[] args) throws IOException {
		ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
		//argsList.add("--disableInternalCommands");
		String[] argsArray = new String[argsList.size()];
		argsArray = argsList.toArray(argsArray);
		Bootstrap.main(argsArray);
	}
}
