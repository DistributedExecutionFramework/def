package at.enfilo.def.manager.webservice.rest;

public class ManagerWebserviceException extends Throwable {
	public ManagerWebserviceException(Throwable cause) {
		super(cause);
	}

	public ManagerWebserviceException(String msg) {
		super(msg);
	}
}
