package at.enfilo.def.communication.api.meta.service;

import at.enfilo.def.communication.api.meta.rest.IMetaService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class MetaServiceClientTest {
	private IMetaServiceClient client;
	private IMetaService metaServiceMock;

	@Before
	public void setUp() throws Exception {
		metaServiceMock = Mockito.mock(IMetaService.class);

		client = MetaServiceClientFactory.createDirectClient(metaServiceMock);
	}

	@Test
	public void getVersion() throws Exception {
		String version = UUID.randomUUID().toString();

		when(metaServiceMock.getVersion()).thenReturn(version);

		String requestedVersion = client.getVersion();
		assertEquals(version, requestedVersion);
	}

	@Test
	public void getTime() throws Exception {
		long timestamp = System.currentTimeMillis();

		when(metaServiceMock.getTime()).thenReturn(timestamp);

		long requestedTimestamp = client.getTime();
		assertEquals(timestamp, requestedTimestamp);
	}
}
