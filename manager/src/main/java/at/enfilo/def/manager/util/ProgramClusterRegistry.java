package at.enfilo.def.manager.util;

import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.manager.impl.UnknownClusterException;
import at.enfilo.def.transfer.UnknownProgramException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramClusterRegistry {

	private final Map<String, String> programClusterMap;
	private final Map<String, IClusterServiceClient> clusterMap;

	private static class ThreadSafeLazySingletonWrapper {
		private static final ProgramClusterRegistry INSTANCE = new ProgramClusterRegistry();
	}

	// Should be implemented as thread safe lazy singleton
	private ProgramClusterRegistry() {
		programClusterMap = new ConcurrentHashMap<>();
		clusterMap = new ConcurrentHashMap<>();
	}

	public static ProgramClusterRegistry getInstance() {
		return ThreadSafeLazySingletonWrapper.INSTANCE;
	}

	public List<String> getClusterIds() {
		return new LinkedList<>(clusterMap.keySet());
	}

	public ServiceEndpointDTO getClusterEndpoint(String cId) throws UnknownClusterException {
		if (clusterMap.containsKey(cId)) {
			return clusterMap.get(cId).getServiceEndpoint();
		}
		throw new UnknownClusterException(String.format("Cluster %s not known", cId));
	}

	public void addCluster(String cId, IClusterServiceClient clusterServiceClient) {
		if (!clusterMap.containsKey(cId)) {
			clusterMap.put(cId, clusterServiceClient);
		}
	}

	public void rebindCluster(String cId, IClusterServiceClient clusterServiceClient) throws UnknownClusterException {
		if (!clusterMap.containsKey(cId)) {
			throw new UnknownClusterException(String.format("Cluster %s not known", cId));
		}
		clusterMap.put(cId, clusterServiceClient);
	}

	public void deleteCluster(String cId) {
		clusterMap.remove(cId);
	}


	public String getClusterId(String pId) throws UnknownProgramException {
		if (programClusterMap.containsKey(pId)) {
			return programClusterMap.get(pId);
		}
		throw new UnknownProgramException(String.format("Program %s not bound to any cluster", pId));
	}

	public void bindProgramToCluster(String pId, String cId) throws UnknownClusterException {
		if (clusterMap.containsKey(cId)) {
			programClusterMap.put(pId, cId);
			return;
		}
		throw new UnknownClusterException(String.format("Can not bind Program %s to unknown cluster %s", pId, cId));
	}

	public void unbindProgram(String pId) {
		programClusterMap.remove(pId);
	}

	public List<String> getProgramIds() {
		return new LinkedList<>(programClusterMap.keySet());
	}

	public boolean isClusterRegistered(String cId) {
		return clusterMap.containsKey(cId);
	}

	public boolean isProgramRegistered(String pId) {
		return programClusterMap.containsKey(pId);
	}

	public IClusterServiceClient getClusterClient(String cId) throws UnknownClusterException {
		if (clusterMap.containsKey(cId)) {
			return clusterMap.get(cId);
		}
		throw new UnknownClusterException(String.format("Cluster %s not known", cId));
	}

}
