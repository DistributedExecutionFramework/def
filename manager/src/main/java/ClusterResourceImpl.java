//package at.enfilo.def.manager.impl;
//
//import at.enfilo.def.manager.api.rest.IClusterResource;
//import at.enfilo.def.manager.util.ProgramClusterRegistry;
//import at.enfilo.def.transfer.dto.IdDTO;
//import at.enfilo.def.transfer.dto.NodeReferenceDTO;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//
///**
// * Created by mase on 07.09.2016.
// */
//public class ClusterResourceImpl implements IClusterResource {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterResourceImpl.class);
//    private static final ProgramClusterRegistry PROGRAM_CLUSTER_REGISTRY = ProgramClusterRegistry.getInstance();
//
//    public ClusterResourceImpl() {
//        // Default constructor
//    }
//
//    @Override
//    public List<IdDTO> getClusterIdList() {
//        return PROGRAM_CLUSTER_REGISTRY.getClusterIdList();
//    }
//
//    @Override
//    public NodeReferenceDTO getClusterReference(String cId) {
//        return PROGRAM_CLUSTER_REGISTRY.getClusterInfo(cId);
//    }
//
//    @Override
//    public void bindCluster(NodeReferenceDTO nodeReferenceDTO) {
//        PROGRAM_CLUSTER_REGISTRY.bindCluster(nodeReferenceDTO.getId(), nodeReferenceDTO);
//        LOGGER.info(
//            "New cluster was registered (bound). Id: {}, REST Socket: {}, Thrift Socket: {}.",
//            nodeReferenceDTO.getId(),
//            nodeReferenceDTO.getRestSocket(),
//            nodeReferenceDTO.getThriftTCPSocket()
//        );
//    }
//
//    @Override
//    public void rebindCluster(NodeReferenceDTO nodeReferenceDTO) {
//        PROGRAM_CLUSTER_REGISTRY.rebindCluster(nodeReferenceDTO.getId(), nodeReferenceDTO);
//        LOGGER.info(
//            "New cluster was registered (rebound). Id: {}, REST Socket: {}, Thrift Socket: {}.",
//            nodeReferenceDTO.getId(),
//            nodeReferenceDTO.getRestSocket(),
//            nodeReferenceDTO.getThriftTCPSocket()
//        );
//    }
//
//    @Override
//    public void unbindCluster(String cId) {
//        PROGRAM_CLUSTER_REGISTRY.unbindCluster(cId);
//        LOGGER.info("Cluster \"{}\" was removed (unbound).", cId);
//    }
//}
