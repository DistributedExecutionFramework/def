package at.enfilo.def.routine.mock;

import at.enfilo.def.routine.StoreRoutine;
import at.enfilo.def.routine.api.Result;

import java.io.IOException;

public class StoreRoutineMock extends StoreRoutine {

	private static StoreRoutineMock lastInstance;

	private boolean run = false;
	private boolean setupStorage = false;
	private boolean shutdownStorage = false;
	private String configFile;

	public StoreRoutineMock() {
		lastInstance = this;
	}

	@Override
	protected Result store(String key, byte[] data, int tupleSeq) throws IOException {
		return new Result();
	}

	@Override
	protected void configure(String configFile) {
		this.configFile = configFile;
	}

	@Override
	protected void setupStorage() throws IOException {
		setupStorage = true;
	}

	@Override
	public void run() {
		run = true;
	}


	@Override
	protected void shutdownStorage() throws IOException {
		shutdownStorage = true;
	}

	public static StoreRoutineMock getLastInstance() {
		return lastInstance;
	}

	public boolean isRun() {
		return run;
	}

	public boolean isSetupStorage() {
		return setupStorage;
	}

	public boolean isShutdownStorage() {
		return shutdownStorage;
	}

	public String getConfigFile() {
		return configFile;
	}
}
