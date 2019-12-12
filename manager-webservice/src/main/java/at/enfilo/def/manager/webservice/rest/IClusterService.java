package at.enfilo.def.manager.webservice.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public interface IClusterService extends IResource {

    /**
     * Fetches and returns all cluster ids available
     *
     * @return  a list with all cluster ids as {@link String}
     */
    @GET
    @Path("/clusterIds")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getAllClusterIds();


    /**
     * Fetches and returns all clusters available
     *
     * @return  a list with all clusters as {@link ClusterInfoDTO}s
     */
    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    List<ClusterInfoDTO> getAllClusters();


    /**
     * Fetches and returns the cluster info of the cluster with the given id
     *
     * @param cId   the cluster ID of the cluster we're interested in
     * @return      the info of the specific cluster as {@link ClusterInfoDTO}
     */
    @GET
    @Path("/clusters/{cId}")
    @Produces(MediaType.APPLICATION_JSON)
    ClusterInfoDTO getClusterInfo(@PathParam("cId") String cId);

    @GET
    @Path("/clusters/{cId}/environment")
    @Produces(MediaType.APPLICATION_JSON)
    List<FeatureDTO> getClusterEnvironment(@PathParam("cId") String cId);

    @GET
    @Path("/clusters/{cId}/workerIds")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getAllWorkerIdsOfCluster(@PathParam("cId") String cId);

    @GET
    @Path("/clusters/{cId}/workers")
    @Produces(MediaType.APPLICATION_JSON)
    List<NodeInfoDTO> getAllWorkersOfCluster(@PathParam("cId") String cId);

    @GET
    @Path("/clusters/{cId}/reducerIds")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getAllReducerIdsOfCluster(@PathParam("cId") String cId);

    @GET
    @Path("/clusters/{cId}/reducers")
    @Produces(MediaType.APPLICATION_JSON)
    List<NodeInfoDTO> getAllReducersOfCluster(@PathParam("cId") String cId);

    @GET
    @Path("/clusters/{cId}/clientRoutineWorkers")
    @Produces(MediaType.APPLICATION_JSON)
    List<NodeInfoDTO> getAllClientRoutineWorkersOfCluster(@PathParam("cId") String cId);

    @GET
    @Path("/clusters/{cId}/nodes/{nId}")
    @Produces(MediaType.APPLICATION_JSON)
    NodeInfoDTO getNodeInfo(@PathParam("cId") String cId, @PathParam("nId") String nId);

    @GET
    @Path("/clusters/{cId}/nodes/{nId}/environment")
    @Produces(MediaType.APPLICATION_JSON)
    List<FeatureDTO> getNodeEnvironment(@PathParam("cId") String cId, @PathParam("nId") String nId);
}
