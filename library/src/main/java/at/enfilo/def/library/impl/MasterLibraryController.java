package at.enfilo.def.library.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.domain.entity.*;
import at.enfilo.def.library.LibraryConfiguration;
import at.enfilo.def.library.api.util.BaseDataTypeRegistry;
import at.enfilo.def.library.api.util.BaseRoutineRegistry;
import at.enfilo.def.library.util.store.IBinaryStoreDriver;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.persistence.api.IFeatureDAO;
import at.enfilo.def.persistence.api.IRoutineDAO;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import at.enfilo.def.persistence.util.PersistenceException;
import at.enfilo.def.transfer.UnknownRoutineException;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.transfer.util.MapManager;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

class MasterLibraryController extends LibraryController {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(MasterLibraryController.class);

	private final PersistenceFacade persistenceFacade;
	private final BaseDataTypeRegistry baseDataTypeRegistry;
	private final BaseRoutineRegistry baseRoutineRegistry;
	private final IBinaryStoreDriver storeDriver;

	MasterLibraryController(
			LibraryConfiguration configuration,
			IBinaryStoreDriver storeDriver,
			PersistenceFacade persistenceFacade,
			BaseDataTypeRegistry baseDataTypeRegistry,
			BaseRoutineRegistry baseRoutineRegistry
	) {
		super(configuration, storeDriver);
		this.persistenceFacade = persistenceFacade;
		this.baseDataTypeRegistry = baseDataTypeRegistry;
		this.baseRoutineRegistry = baseRoutineRegistry;
		this.storeDriver = storeDriver;
	}

	@Override
	protected void init() {
		LOGGER.info("Persisting auto-detected data-types and routines.");

		try {
			// Persisting known data-types.
//			// batchSaveOrUpdate does not work here.
//			Collection<DataTypeDTO> detectedDataTypes = baseDataTypeRegistry.getAll();
//			this.persistenceFacade.getNewDataTypeDAO().batchSaveOrUpdate(
//				MapManager.map(detectedDataTypes, DataType.class).collect(Collectors.toList())
//			);
			for (DataTypeDTO baseDataType : baseDataTypeRegistry.getAll()) {
				this.persistenceFacade.getNewDataTypeDAO().saveOrUpdate(
						MapManager.map(baseDataType, DataType.class)
				);
			}

			// Persisting known routines.
//			// batchSaveOrUpdate does not work here.
//			Collection<RoutineDTO> detectedRoutines = baseRoutineRegistry.getAll();
//			this.persistenceFacade.getNewRoutineDAO().batchSaveOrUpdate(
//				MapManager.map(detectedRoutines, Routine.class).collect(Collectors.toList())
//			);

			for (RoutineDTO detectedRoutine : baseRoutineRegistry.getAll()) {
				Routine r = MapManager.map(detectedRoutine, Routine.class);
				if (r.getRequiredFeatures() != null) {
					for (Feature f : r.getRequiredFeatures()) {
						this.persistenceFacade.getNewFeatureDAO().saveOrUpdate(f);
					}
				}
				this.persistenceFacade.getNewRoutineDAO().saveOrUpdate(r);
				LOGGER.info("Routine {} inserted.", r.getName());
			}

		} catch (Exception e) {
			LOGGER.warn("Failed to create base Routines, DataTypes, Features", e);
		}
	}


	@Override
	protected RoutineDTO fetchRoutine(String rId) throws RoutineFetchException {
		LOGGER.info("Fetch Routine {} from Database.", rId);
		try {
			IRoutineDAO routineDAO = persistenceFacade.getNewRoutineDAO();

			Routine routine = routineDAO.findById(rId);
			routineDAO.forceLoadLazyField(routine, Routine::getRoutineBinaries);
			routineDAO.forceLoadLazyField(routine, Routine::getInParameters);
			routineDAO.forceLoadLazyField(routine, Routine::getArguments);
			routine.getRoutineBinaries().forEach(rb -> rb.setData(null));

			routine.setRequiredFeatures(new HashSet<>(mergeFeatures(routine.getRequiredFeatures())));
			return MapManager.map(routine, RoutineDTO.class);
		}
		catch (Exception e) {
			LOGGER.error("Error while fetch Routine from Database.", e);
			throw  new RoutineFetchException(e);
		}
	}

	@Override
	protected void updateLibraryEndpoint(ServiceEndpointDTO serviceEndpoint) {
	}

	@Override
	public List<String> findRoutines(String pattern) throws PersistenceException {
		if (pattern == null || pattern.isEmpty()) {
			pattern = "%";
		} else {
			pattern = pattern.replace('*', '%');
		}
		return persistenceFacade.getNewRoutineDAO().findIdsByNameOrDescription(pattern);
	}

	@Override
	public void removeRoutine(String rId) throws PersistenceException {
		persistenceFacade.getNewRoutineDAO().deleteById(rId);
		RoutineDTO routine = routineRegistry.remove(rId);
		if (routine != null) {
			for (RoutineBinaryDTO binary : routine.getRoutineBinaries()) {
				try {
					try {
						RoutineBinary tmp = persistenceFacade.getNewRoutineBinaryDAO().findById(binary.getId());
						if (tmp == null) {
							LOGGER.debug("Remove RoutineBinary {} from storage.", binary.getId());
							storeDriver.delete(binary.getId());
						}
					} catch (PersistenceException e) {
						// Assume RoutineBinary no longer exists, delete it.
						LOGGER.debug("Remove RoutineBinary {} from storage.", binary.getId());
						storeDriver.delete(binary.getId());
					}
				} catch (IOException e1) {
					LOGGER.warn("Can not delete RoutineBinary {}: {}.", binary.getId(), e1.getMessage(), e1);
				}
			}
		}
		LOGGER.info("Routine {} and all binaries removed successfully.", rId);
	}

	@Override
	public String createRoutine(RoutineDTO routineDTO) throws ExecutionException {
		LOGGER.debug("Try to create new routine.");
		try {
			Set<Feature> features = fetchFeaturesForRoutine(routineDTO);
			Routine routine = MapManager.map(routineDTO, Routine.class);
			routine.setRequiredFeatures(features);
			String rId = persistenceFacade.getNewRoutineDAO().save(routine);
			routineDTO.setId(rId);
			routineRegistry.put(rId, routineDTO);
			LOGGER.info("New routine created with id {}.", rId);
			return rId;

		} catch (PersistenceException e) {
			LOGGER.error("Error while create new routine.", e);
			throw new ExecutionException(e);
		}
	}

	private Set<Feature> fetchFeaturesForRoutine(RoutineDTO routineDTO) throws ExecutionException {
		//IFeatureDAO featureDAO = persistenceFacade.getNewFeatureDAO();
		if (routineDTO.getRequiredFeatures() == null || routineDTO.getRequiredFeatures().isEmpty()) {
			throw new ExecutionException(new PersistenceException("No features given for routine."));
		}
		Set<Feature> features = new HashSet<>();
		for (FeatureDTO featureDTO : routineDTO.getRequiredFeatures()) {
			//Feature feature = featureDAO.findById(featureDTO.getId());
			Feature feature = new Feature(featureDTO);
			features.add(feature);
			if (feature.getSubFeatures() != null) {
				features.addAll(feature.getSubFeatures());
			}
			/*
			featureDTO.setVersion(feature.getVersion());
			featureDTO.setGroup(feature.getGroup());
			featureDTO.setName(feature.getName());
			if (featureDTO.getExtensions() != null && !featureDTO.getExtensions().isEmpty()) {
				for (FeatureDTO extensionDTO : featureDTO.getExtensions()) {
					Feature extension = featureDAO.findById(extensionDTO.getId());
					features.add(extension);
					extensionDTO.setName(extension.getName());
					extensionDTO.setGroup(extension.getGroup());
					extensionDTO.setVersion(extension.getVersion());
					extensionDTO.setBaseId(extension.getBaseFeature().getId());
				}
			}
			*/
		}
		return features;
	}

	@Override
	public String updateRoutine(RoutineDTO routineDTO) throws ExecutionException {
		try {
			Set<Feature> features = fetchFeaturesForRoutine(routineDTO);
			Routine routine = MapManager.map(routineDTO, Routine.class);
			routine.setRequiredFeatures(features);
			return persistenceFacade.getNewRoutineDAO().saveNewRevision(
					routineDTO.getId(),
					routine
			);

		} catch (Exception e) {
			LOGGER.error("Error while update Routine.", e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public String uploadRoutineBinary(String rId, String md5, long sizeInBytes, boolean isPrimary, ByteBuffer data) throws ExecutionException {
		try {
			IRoutineDAO routineDAO = persistenceFacade.getNewRoutineDAO();
			Routine routine = routineDAO.findById(rId);
			if (routine == null) {
				throw new UnknownRoutineException(String.format("Routine with Id %s not exists.", rId));
			}

			RoutineBinary routineBinary = new RoutineBinary();
			routineBinary.setId(UUID.randomUUID().toString());
			routineBinary.setMd5(md5);
			routineBinary.setSizeInBytes(sizeInBytes);
			routineBinary.setPrimary(isPrimary);
			routineBinary.setData(data.array());

			// Persist RoutineBinary
			RoutineBinaryDTO binaryToPersist = MapManager.map(routineBinary, RoutineBinaryDTO.class);
			URL url = storeDriver.store(binaryToPersist);

			// Update DB
			routineBinary.setUrl(url.toString());
			List<RoutineBinary> routineBinaries = routine.getRoutineBinaries();
			routineBinaries.add(routineBinary);
			routineDAO.saveOrUpdate(routine);

			// Update cache
			RoutineBinaryDTO cacheBinary = new RoutineBinaryDTO();
			cacheBinary.setId(routineBinary.getId());
			cacheBinary.setPrimary(routineBinary.isPrimary());
			cacheBinary.setUrl(routineBinary.getUrl());
			cacheBinary.setMd5(routineBinary.getMd5());
			cacheBinary.setSizeInBytes(routineBinary.getSizeInBytes());
			if (!routineRegistry.containsKey(rId)) {
				routineRegistry.put(rId, MapManager.map(routine, RoutineDTO.class));
			}
			routineRegistry.get(rId).addToRoutineBinaries(cacheBinary);

			return routineBinary.getId();

		} catch (UnknownRoutineException | IOException e) {
			LOGGER.error(String.format("Error while add RoutineBinary to Routine %s.", rId), e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public void removeRoutineBinary(String rId, String bId) throws ExecutionException {
		try {
			IRoutineDAO routineDAO = persistenceFacade.getNewRoutineDAO();
			Routine routine = routineDAO.findById(rId);
			Iterator<RoutineBinary> it = routine.getRoutineBinaries().iterator();
			while (it.hasNext()) {
				RoutineBinary binary = it.next();
				if (binary.getId().equalsIgnoreCase(bId)) {
					it.remove();
					break;
				}
			}
			routineDAO.save(routine);

			// Update cache
			routineRegistry.put(rId, MapManager.map(routine, RoutineDTO.class));

			// TODO Remove binary if it's not used by other routines
			// Removing from store.
			//storeDriver.delete(bId);

		} catch (PersistenceException /*| IOException*/ e) {
			LOGGER.error("Error while delete RoutineBinary {}.", bId, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public List<String> findDataTypes(String searchPattern) throws ExecutionException {
		try {
			if (searchPattern == null || searchPattern.isEmpty()) {
				return persistenceFacade.getNewDataTypeDAO().findAllIds();
			}
			searchPattern = searchPattern.replace('*', '%');
			return persistenceFacade.getNewDataTypeDAO().findIdsByNameOrDescription(searchPattern);

		} catch (PersistenceException e) {
			LOGGER.error("Error while find DataTypes.", e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public String createDataType(String name, String schema) throws ExecutionException {
		try {
			DataType dataType = new DataType();
			dataType.setName(name);
			schema = schema.replaceAll("_id = .+,", "_id = \"" + dataType.getId() + "\",");
			dataType.setSchema(schema);

			return persistenceFacade.getNewDataTypeDAO().save(dataType);

		} catch (PersistenceException e) {
			LOGGER.error("Error while create DataType '{}'.", name, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public DataTypeDTO getDataType(String dId) throws ExecutionException {
		try {
			DataType dataType = persistenceFacade.getNewDataTypeDAO().findById(dId);
			return MapManager.map(dataType, DataTypeDTO.class);
		} catch (PersistenceException e) {
			LOGGER.error("Error while get/fetch DataType {}.", dId, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public void removeDataType(String dId) throws ExecutionException {
		try {
			persistenceFacade.getNewDataTypeDAO().deleteById(dId);
		} catch (PersistenceException e) {
			LOGGER.error("Error while remove DataType {}.", dId, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public List<TagDTO> findTags(String searchPattern) throws ExecutionException {
		try {
			List<Tag> tagList = persistenceFacade.getNewTagDAO().findByLabelOrDescription(searchPattern);
			return MapManager.map(tagList, TagDTO.class).collect(Collectors.toList());
		} catch (PersistenceException e) {
			LOGGER.error("Error while find Tags with pattern '{}'.", searchPattern, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public String createTag(String label, String description) throws ExecutionException {
		try {
			Tag tag = new Tag();
			tag.setId(label);
			tag.setDescription(description);

			return persistenceFacade.getNewTagDAO().save(tag);

		} catch (PersistenceException e) {
			LOGGER.error("Error while create Tag with label '{}'.", label, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public void removeTag(String label) throws ExecutionException {
		try {
			persistenceFacade.getNewTagDAO().deleteById(label);
		} catch (PersistenceException e) {
			LOGGER.error("Error while remove Tag with label '{}'.", label, e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public RoutineBinaryDTO getRoutineBinary(String bId) throws ExecutionException {
		try {
			LOGGER.debug("RoutineBinary get request: {}", bId);
			if (storeDriver.exists(bId)) {
				try {
					return storeDriver.read(bId);
				} catch (IOException | NoSuchAlgorithmException e) {
					LOGGER.error("Error while fetch RoutineBinary {}.", bId, e);
					throw e;
				}
			} else {
				RoutineBinary routineBinary = persistenceFacade.getNewRoutineBinaryDAO().findById(bId);
				if (routineBinary != null) {
					return storeDriver.read(bId, new URL(routineBinary.getUrl()));
				}
			}

			LOGGER.error("RoutineBinary {} not exists.", bId);
			throw new RoutineBinaryNotExists("RoutineBinary " + bId + " not exists.");

		} catch (RoutineBinaryNotExists | NoSuchAlgorithmException | IOException e) {
			throw new ExecutionException(e);
		}
	}


	@Override
	public String createFeature(String name, String group, String version) throws ExecutionException {
		LOGGER.debug("Trying to create new feature.");
		try {
			Feature feature = new Feature();
			feature.setName(name);
			feature.setVersion(version);
			feature.setGroup(group);
			return persistenceFacade.getNewFeatureDAO().save(feature);
		} catch (PersistenceException e) {
			LOGGER.error("Error while creating new feature.", e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public String addExtension(String featureId, String name, String version) throws ExecutionException {
		LOGGER.debug("Trying to add extension to feature ID: ", featureId);
		try {
			IFeatureDAO featureDAO = persistenceFacade.getNewFeatureDAO();
			Feature feature = featureDAO.findById(featureId);

			if (feature == null) {
				throw new PersistenceException("Feature with given id does not exist");
			}
			if (feature.getSubFeatures() == null) {
				feature.setSubFeatures(new ArrayList<>());
			}
			Feature extension = new Feature();
			extension.setName(name);
			extension.setVersion(version);
			extension.setBaseFeatureId(featureId);
			feature.getSubFeatures().add(extension);

			return featureDAO.save(extension);
		} catch (PersistenceException e) {
			LOGGER.error("Error while adding extension.", e);
			throw new ExecutionException(e);
		}
	}

	@Override
	public List<FeatureDTO> getFeatures(String pattern) throws ExecutionException {
		try {
			IFeatureDAO featureDAO = persistenceFacade.getNewFeatureDAO();
			List<Feature> featureList;
			if (pattern == null || pattern.isEmpty()) {
				featureList = featureDAO.findAllBaseFeatures();
			} else {
				featureList = mergeFeatures(featureDAO.findByNameOrVersion(pattern));
			}
			return MapManager.map(featureList, FeatureDTO.class).collect(Collectors.toList());
		} catch (PersistenceException e) {
			LOGGER.error("Error while finding Features with pattern '{}'.", pattern, e);
			throw new ExecutionException(e);
		}
	}

	private List<Feature> mergeFeatures(Collection<Feature> features) {
		Map<String, Feature> result = new HashMap<>();
		Queue<Feature> featureQueue = new LinkedList<>(features);
		int timeout = 10; //In case there is broken data, there could be an endless loop
		while (!featureQueue.isEmpty() && timeout > 0) {
			timeout--;
			Feature feature = featureQueue.poll();
			if (feature.getBaseFeature() == null) {
				if (result.containsKey(feature.getId())) {
					continue;
				}
				Feature newFeature = new Feature(feature.getId(), feature.getName(), feature.getVersion(), feature.getGroup());
				newFeature.setSubFeatures(new ArrayList<>());
				result.put(newFeature.getId(), newFeature);
			} else {
				Feature baseFeature = feature.getBaseFeature();
				if (result.containsKey(baseFeature.getId())) {
					Feature newFeature = new Feature(feature.getId(), feature.getName(), feature.getVersion(), feature.getGroup());
					newFeature.setSubFeatures(new ArrayList<>());
					newFeature.setBaseFeature(baseFeature);
					result.get(baseFeature.getId()).getSubFeatures().add(newFeature);
				} else {
					featureQueue.add(baseFeature);
					featureQueue.add(feature);
				}
			}
		}

		return new ArrayList<>(result.values());
	}
}
