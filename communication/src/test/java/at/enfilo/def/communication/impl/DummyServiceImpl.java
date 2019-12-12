package at.enfilo.def.communication.impl;

public class DummyServiceImpl implements DummyService.Iface, IDummyService {
	public static final String RESPONSE = "pong";

	@Override
	public String ping() {
		return RESPONSE;
	}
}
