package at.enfilo.def.client.api;

public class ExtractDataTypeException extends Exception {
	public ExtractDataTypeException(Exception e) {
		super(e);
	}

	public ExtractDataTypeException(String msg) {
		super(msg);
	}
}
