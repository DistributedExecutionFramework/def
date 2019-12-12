package at.enfilo.def.communication.impl.meta;

import at.enfilo.def.communication.api.meta.rest.IMetaService;
import at.enfilo.def.communication.api.meta.thrift.MetaService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class MetaServiceImpl implements IMetaService, MetaService.Iface {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(MetaServiceImpl.class);

	private static final String MANIFEST = "META-INF/MANIFEST.MF";
	private static final String VERSION_ATTR = "DEF-Version";
	private static final String UNKNOWN_VERSION = "unknown";

	@Override
	public String getVersion() {
		try {
			Enumeration<URL> resources = getClass().getClassLoader().getResources(MANIFEST);
			while (resources.hasMoreElements()) {
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				if (manifest.getMainAttributes().containsKey(VERSION_ATTR)) {
					return manifest.getMainAttributes().getValue(VERSION_ATTR);
				}
			}
		} catch (IOException e) {
			LOGGER.error("Error while fetch version from {}", MANIFEST, e);
		}
		return UNKNOWN_VERSION;
	}

	@Override
	public long getTime() {
		return System.currentTimeMillis();
	}
}
