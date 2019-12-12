//package at.enfilo.def.manager.impl;
//
//import at.enfilo.def.communication.dto.Protocol;
//import at.enfilo.def.communication.dto.ServiceEndpointDTO;
//import at.enfilo.def.manager.api.rest.IProgramResource;
//import at.enfilo.def.manager.util.ProgramClusterRegistry;
//import at.enfilo.def.persistence.api.IPersistenceFacade;
//import at.enfilo.def.persistence.util.PersistenceException;
//import at.enfilo.def.transfer.dto.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Collections;
//import java.util.List;
//
///**
// * Created by mase on 16.08.2016.
// */
//public class ProgramResourceImpl implements IProgramResource {
//
//	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramResourceImpl.class);
//    private static final ProgramClusterRegistry PROGRAM_CLUSTER_REGISTRY = ProgramClusterRegistry.getInstance();
//
//	private final IPersistenceFacade persistenceFacade;
//	//private final Function<ServiceEndpointDTO, IClient<DelegateResource.Client>> clusterClientBuilder;
//
//	public ProgramResourceImpl(IPersistenceFacade persistenceFacade) {
//		this.persistenceFacade = persistenceFacade;
//
////		this.clusterClientBuilder = (serviceEndpoint) -> new ThriftTCPClient<>(
////            serviceEndpoint,
////            IDelegateResource.class.getName(),
////			DelegateResource.Client::new
////        );
//	}
//
//	@Override
//	public List<IdDTO> getAllPrograms(String uId) {
//		try {
//			return persistenceFacade.getNewUserDAO().getAllProgramIds(uId);
//
//		} catch (PersistenceException e) {
//			LOGGER.error("Error occurred while fetching all program ids for [uId: {}].", uId, e);
//			return Collections.emptyList();
//		}
//	}
//
//	@Override
//	public IdDTO createNewProgram(String cId) {
//
//        NodeReferenceDTO nodeReferenceDTO = PROGRAM_CLUSTER_REGISTRY.getClusterInfo(cId);
//
////        if (nodeReferenceDTO != null) try {
////
////            IClient<DelegateResource.Client> clusterClient = clusterClientBuilder.apply(
////				toServiceEndpoint(nodeReferenceDTO)
////			);
////
////			IdDTO programIdDTO = clusterClient.execute(
////				DelegateResource.Client::createNewProgram
////            );
////
////            PROGRAM_CLUSTER_REGISTRY.registerProgramClusterPair(programIdDTO.getId(), cId);
////            return programIdDTO;
////
////        } catch (ClientCommunicationException e) {
////            LOGGER.error("Error occurred while creating new program on cluster [cId: {}].", cId, e);
////        }
//
//        return null;
//    }
//
//	@Override
//	public ProgramDTO getProgram(String pId) {
//		try {
//
//			return persistenceFacade.getNewProgramDAO().getProgram(pId);
//
//		} catch (PersistenceException e) {
//			LOGGER.error("Error occurred while fetching program info. [pId: {}]", pId, e);
//			return null;
//		}
//	}
//
//	@Override
//	public ProgramDTO deleteProgram(String pId) {
//		return null;
//	}
//
//	@Override
//	public BooleanDTO markProgramAsFinished(String pId) {
//		return null;
//	}
//
//	@Override
//	public List<IdDTO> getAllJobs(String pId) {
//		try {
//
//			return persistenceFacade.getNewProgramDAO().getAllJobIds(pId);
//
//		} catch (PersistenceException e) {
//			LOGGER.error("Error occurred while fetching all job ids for [pId: {}].", pId, e);
//			return Collections.emptyList();
//		}
//	}
//
//	@Override
//	public IdDTO createNewJob(String pId) {
//
//        String cId = PROGRAM_CLUSTER_REGISTRY.getClusterId(pId);
//        NodeReferenceDTO nodeReferenceDTO = PROGRAM_CLUSTER_REGISTRY.getClusterInfo(cId);
//
////        if (nodeReferenceDTO != null) try {
////
////            IClient<DelegateResource.Client> clusterClient = clusterClientBuilder.apply(
////					toServiceEndpoint(nodeReferenceDTO)
////			);
////
////			return clusterClient.execute(client -> client.createNewJob(pId));
////
////        } catch (ClientCommunicationException e) {
////            LOGGER.error(
////                "Error occurred while crating new job on cluster [cId: {}], on program [pId: {}].",
////                cId,
////                pId,
////                e
////            );
////        }
//
//        return null;
//    }
//
//	@Override
//	public JobDTO getJob(String pId, String jId) {
//		try {
//
//			return persistenceFacade.getNewJobDAO().getJob(jId);
//
//		} catch (PersistenceException e) {
//			LOGGER.error("Error occurred while fetching job info. [jId: {}]", jId, e);
//			return null;
//		}
//	}
//
//	@Override
//	public JobDTO deleteJob(String pId, String jId) {
//		return null;
//	}
//
//	@Override
//	public IdDTO getAttachedMapLibrary(String pId, String jId) {
//		return null;
//	}
//
//	@Override
//	public BooleanDTO attachMapLibraryToJob(String pId, String jId, String mlId) {
//		return null;
//	}
//
//	@Override
//	public IdDTO getAttachedCombineLibrary(String pId, String jId) {
//		return null;
//	}
//
//	@Override
//	public BooleanDTO attachCombineLibraryToJob(String pId, String jId, String clId) {
//		return null;
//	}
//
//	@Override
//	public IdDTO getAttachedReduceLibrary(String pId, String jId) {
//		return null;
//	}
//
//	@Override
//	public BooleanDTO attachReduceLibraryToJob(String pId, String jId, String rlId) {
//		return null;
//	}
//
//	@Override
//	public List<IdDTO> getAllTasks(String pId, String jId) {
//		try {
//
//			return persistenceFacade.getNewJobDAO().getAllTaskIds(jId);
//
//		} catch (PersistenceException e) {
//			LOGGER.error("Error occurred while fetching all task ids for [jId: {}].", jId, e);
//			return Collections.emptyList();
//		}
//	}
//
//	@Override
//	public IdDTO createNewTask(String pId, String jId, String rId) {
//
//        String cId = PROGRAM_CLUSTER_REGISTRY.getClusterId(pId);
//        NodeReferenceDTO nodeReferenceDTO = PROGRAM_CLUSTER_REGISTRY.getClusterInfo(cId);
//
////        if (nodeReferenceDTO != null) try {
////
////            IClient<DelegateResource.Client> clusterClient = clusterClientBuilder.apply(
////            		toServiceEndpoint(nodeReferenceDTO)
////			);
////
////			// TODO: Change interface; only Id is needed
////			return clusterClient.execute(client -> client.createNewTask(pId, jId, null));
////
////        } catch (ClientCommunicationException e) {
////            LOGGER.error(
////                "Error occurred while crating new task on cluster [cId: {}], on program [pId: {}], on job [jId: {}].",
////                cId,
////                pId,
////                jId,
////                e
////            );
////        }
//
//        return null;
//    }
//
//	@Override
//	public TaskDTO getTask(String pId, String jId, String tId) {
//		try {
//
//			return persistenceFacade.getNewTaskDAO().getTask(tId);
//
//		} catch (PersistenceException e) {
//			LOGGER.error("Error occurred while fetching task info. [tId: {}]", tId, e);
//			return null;
//		}
//	}
//
//	@Override
//	public BooleanDTO markJobAsComplete(String pId, String jId) {
//		return null;
//	}
//
//	@Override
//	public BooleanDTO abortJob(String pId, String jId) {
//		return null;
//	}
//
//	@Override
//	public List<IdDTO> getAllSharedResources(String pId) {
//		return Collections.emptyList();
//	}
//
//	@Override
//	public IdDTO createNewSharedResource(String pId, DataTypeDTO dType) {
//		return null;
//	}
//
//	@Override
//	public ResourceDTO getSharedResourceInfo(String pId, String rId) {
//		return null;
//	}
//
//	@Override
//	public void deleteSharedResource(String pId, String rId) {
//	}
//
//	@Deprecated
//	ServiceEndpointDTO toServiceEndpoint(NodeReferenceDTO nodeReference) {
//		return new ServiceEndpointDTO(
//			nodeReference.getThriftTCPSocket().getHost(),
//			nodeReference.getThriftTCPSocket().getPort(),
//			Protocol.THRIFT_TCP
//		);
//	}
//}
