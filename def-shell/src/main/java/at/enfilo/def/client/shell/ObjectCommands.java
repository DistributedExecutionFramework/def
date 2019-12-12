package at.enfilo.def.client.shell;

import at.enfilo.def.client.shell.formatter.ShellOutputFormatter;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.datatype.DEFBoolean;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.datatype.DEFString;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static at.enfilo.def.client.shell.Constants.*;

/**
 * Handling commands for 'object ...' or 'o ...'
 *
 */
@Component
public class ObjectCommands implements CommandMarker {
	private final Map<String, Object> objectMap = new HashMap<>();

	enum ObjectType {
		SERVICE_ENDPOINT,
		TASK,
		JOB,
		PROGRAM,
		RESOURCE,
		ROUTINE_INSTANCE,
		DEFDouble,
		DEFInteger,
		DEFString,
		DEFBoolean,
		AWS_SPECIFICATION
	}


	/**
	 * List all avail objects.
	 *
	 * @return list of objects
	 */
	@CliCommand(value = CMD_OBJECT_LIST, help = "List all local objects")
	public String listObjects() {
		StringBuilder sb = new StringBuilder();
		objectMap.entrySet().forEach(
				entry -> sb.append(entry.getKey()).append(": ").append(entry.getValue().getClass().getSimpleName()).append("\n")
		);
		return sb.toString();
	}


	/**
	 * Creates a new object.
	 * @param type - object type
	 * @param name - object name (should be unique)
	 * @return
	 */
	@CliCommand(value = CMD_OBJECT_CREATE, help = "Create a new object")
	public String createObject(
		@CliOption(key = OPT_TYPE, mandatory = true, help = "Type of object") final ObjectType type,
		@CliOption(key = OPT_NAME, mandatory = true, help = "Name of object") final String name
	) {
		if (objectMap.containsKey(name)) {
			throw new IllegalArgumentException(String.format(MESSAGE_OBJECT_ALREADY_EXISTS, name));
		}

		Object object;
		switch (type) {
			case JOB:
				object = new JobDTO();
				break;
			case PROGRAM:
				object = new ProgramDTO();
				break;
			case TASK:
				object = new TaskDTO();
				break;
			case SERVICE_ENDPOINT:
				object = new ServiceEndpointDTO();
				break;
			case RESOURCE:
				object = new ResourceDTO();
				break;
			case ROUTINE_INSTANCE:
				object = new RoutineInstanceDTO();
				break;
			case DEFDouble:
				object = new DEFDouble();
				break;
			case DEFString:
				object = new DEFString();
				break;
			case DEFBoolean:
				object = new DEFBoolean();
				break;
			case DEFInteger:
				object = new DEFInteger();
				break;
			case AWS_SPECIFICATION:
				object = new AWSSpecificationDTO();
				break;
			default:
				throw new IllegalArgumentException("Object Type " + type + " not supported");
		}
		objectMap.put(name, object);
		return String.format(MESSAGE_OBJECT_CREATED, type, name);
	}


	/**
	 * Shows/prints the specified object.
	 * @param name - object name
	 * @return formatted object
	 */
	@CliCommand(value = CMD_OBJECT_SHOW, help = "Show all information of an object")
	public String showObject(
		@CliOption(key = OPT_NAME, mandatory = true, help = "Name of object") final String name
	) {
		if (objectMap.containsKey(name)) {
			return ShellOutputFormatter.format(objectMap.get(name));
		} else {
			throw new UnknownObjectException(String.format(MESSAGE_OBJECT_NOT_EXISTS, name));
		}
	}


	/**
	 * Deletes the specified object
	 * @param name - name of object
	 * @return success/failed message
	 */
	@CliCommand(value = CMD_OBJECT_REMOVE, help = "Delete an object")
	public String deleteObject(
		@CliOption(key = OPT_NAME, mandatory = true, help = "Name of object") final String name
	) {
		if (objectMap.containsKey(name)) {
			objectMap.remove(name);
			return String.format(MESSAGE_OBJECT_REMOVE, name);
		} else {
			throw new UnknownObjectException(String.format(MESSAGE_OBJECT_NOT_EXISTS, name));
		}
	}


	/**
	 * Update ServicePoint Object
	 * @param name - name of ServicePoint Object
	 * @param host - new host or null
	 * @param port - new port or null
	 * @param protocol - new protocol or null
	 * @param pattern - new pattern or null
	 * @return update message
	 */
	@CliCommand(value = CMD_OBJECT_UPDATE_ENDPOINT, help = "Update values of a ServiceEndpoint Object")
	public String updateServiceEndpoint(
		@CliOption(key = OPT_NAME, mandatory = true, help = "Name of object") final String name,
		@CliOption(key = OPT_HOST, help = "Host") final String host,
		@CliOption(key = OPT_PORT, help = "Port") final Integer port,
		@CliOption(key = OPT_PROTOCOL, help = "Protocol") final Protocol protocol,
		@CliOption(key = OPT_URL_PATTERN, help = "URL pattern") final String pattern
	) {

		ServiceEndpointDTO endpoint = getObject(name, ServiceEndpointDTO.class);

		if (host != null) {
			endpoint.setHost(host);
		}
		if (port != null) {
			endpoint.setPort(port);
		}
		if (protocol != null) {
			endpoint.setProtocol(protocol);
		}
		if (pattern != null) {
			endpoint.setPathPrefix(pattern);
		}

		return String.format(MESSAGE_OBJECT_UPDATED, name);
	}

	@CliCommand(value = CMD_OBJECT_UPDATE_AWS_SPECIFICATION, help = "Update values of an AWSSpecification Object")
	public String updateAWSspecification(
			@CliOption(key = OPT_NAME, help = "Name of object", mandatory = true) final String name,
			@CliOption(key = OPT_AWS_ACCESS_KEY_ID, help = "Access key ID for AWS") final String accessKeyID,
			@CliOption(key = OPT_AWS_SECRET_KEY, help = "Secret key ID for AWS") final String secretKey,
			@CliOption(key = OPT_AWS_REGION, help = "Region for AWS") final String region,
			@CliOption(key = OPT_AWS_PUBLIC_SUBNET_ID, help = "Public subnet ID for AWs") final String publicSubnetId,
			@CliOption(key = OPT_AWS_PRIVATE_SUBNET_ID, help = "Private subnet ID for AWS") final String privateSubnetId,
			@CliOption(key = OPT_AWS_VPC_ID, help = "VPC id for AWS") final String vpcId,
			@CliOption(key = OPT_AWS_KEYPAIR_NAME, help = "Key pair name for AWS") final String keypairName,
			@CliOption(key = OPT_VPN_DYNAMIC_IP_NETWORK_ADDRESS, help = "Dynamic IP network address for VPN") final String vpnDynamicIpNetworkAddress,
			@CliOption(key = OPT_VPN_DYNAMIC_IP_SUBNET_MASK, help = "Dynamic IP subnet maks for VPN") final String vpnDynamicIpSubnetMaskSuffix,
			@CliOption(key = OPT_CLUSTER_IMAGE_ID, help = "Cluster image ID for AWS") final String clusterImageId,
			@CliOption(key = OPT_CLUSTER_INSTANCE_SIZE, help = "Cluster instance size for AWS") final String clusterInstanceSize,
			@CliOption(key = OPT_WORKER_IMAGE_ID, help = "Worker image ID for AWS") final String workerImageId,
			@CliOption(key = OPT_WORKER_INSTANCE_SIZE, help = "Worker instance size for AWS") final String workerInstanceSize,
			@CliOption(key = OPT_REDUCER_IMAGE_ID, help = "Reducer image ID for AWS") final String reducerIamgeId,
			@CliOption(key = OPT_REDUCER_INSTANCE_SIZE, help = "Reducer instance size for AWS") final String reducerInstanceSize
	) {
		AWSSpecificationDTO awsSpecification = getObject(name, AWSSpecificationDTO.class);

		if (accessKeyID != null) {
			awsSpecification.setAccessKeyID(accessKeyID);
		}
		if (secretKey != null) {
			awsSpecification.setSecretKey(secretKey);
		}
		if (region != null) {
			awsSpecification.setRegion(region);
		}
		if (publicSubnetId != null) {
			awsSpecification.setPublicSubnetId(publicSubnetId);
		}
		if (privateSubnetId != null) {
			awsSpecification.setPrivateSubnetId(privateSubnetId);
		}
		if (vpcId != null) {
			awsSpecification.setVpcId(vpcId);
		}
		if (keypairName != null) {
			awsSpecification.setKeypairName(keypairName);
		}
		if (vpnDynamicIpNetworkAddress != null) {
			awsSpecification.setVpnDynamicIpNetworkAddress(vpnDynamicIpNetworkAddress);
		}
		if (vpnDynamicIpSubnetMaskSuffix != null) {
			awsSpecification.setVpnDynamicIpSubnetMaskSuffix(Integer.parseInt(vpnDynamicIpSubnetMaskSuffix));
		}
		if (clusterImageId != null) {
			awsSpecification.setClusterImageId(clusterImageId);
		}
		if (clusterInstanceSize != null) {
			awsSpecification.setClusterInstanceSize(clusterInstanceSize);
		}
		if (workerImageId != null) {
			awsSpecification.setWorkerImageId(workerImageId);
		}
		if (workerInstanceSize != null) {
			awsSpecification.setWorkerInstanceSize(workerInstanceSize);
		}
		if (reducerIamgeId != null) {
			awsSpecification.setReducerImageId(reducerIamgeId);
		}
		if (reducerInstanceSize != null) {
			awsSpecification.setReducerInstanceSize(reducerInstanceSize);
		}
		return String.format(MESSAGE_OBJECT_UPDATED, name);
	}

	/**
	 * Update RoutineInstance Object
	 * @param name - name of ServicePoint Object
	 * @return update message
	 */
	@CliCommand(value = CMD_OBJECT_UPDATE_ROUTINE_INSTANCE, help = "Update values of a RoutineInstance Object")
	public String updateRoutineInstance(
			@CliOption(key = OPT_NAME, mandatory = true, help = "Name of object") final String name,
			@CliOption(key = OPT_ROUTINE_ID, help = "Routine Id") final String rId,
			@CliOption(key = OPT_IN_PARAMS, help = "Input parameter as map: <param-name1:resource-obj>,<param-name2:resource-obj>,...") final String[] inParamMap,
			@CliOption(key = OPT_MISSING_PARAMS, help = "Missing input parameter indexes: <param-name1>,<param-name2>,...") final String[] missingParams
	) {

		RoutineInstanceDTO instance = getObject(name, RoutineInstanceDTO.class);

		if (rId != null) {
			instance.setRoutineId(rId);
		}
		if (inParamMap != null) {
			instance.setInParameters(new HashMap<>());
			for (String param : inParamMap) {
				String[] splittedParam = param.split(":");
				instance.putToInParameters(splittedParam[0], getObject(splittedParam[1], ResourceDTO.class));
			}
		}
		if (missingParams != null) {
			instance.setMissingParameters(new LinkedList<>());
			for (String missingParam : missingParams) {
				instance.addToMissingParameters(missingParam);
			}
		}

		return String.format(MESSAGE_OBJECT_UPDATED, name);
	}


	@CliCommand(
			value = {CMD_OBJECT_UPDATE_DEF_BOOLEAN, CMD_OBJECT_UPDATE_DEF_INTEGER, CMD_OBJECT_UPDATE_DEF_DOUBLE, CMD_OBJECT_UPDATE_DEF_STRING},
			help = "Update value of a DEF Scalar datatype.")
	public String updateDEFScalar(
		@CliOption(key = OPT_NAME, mandatory = true, help = "Name of object") final String name,
		@CliOption(key = OPT_VALUE, mandatory = true, help = "Value of object") final String value
	) {
		Object o = objectMap.get(name);
		if (DEFString.class.isInstance(o)) {
			DEFString.class.cast(o).setValue(value);
		} else if (DEFBoolean.class.isInstance(o)) {
			DEFBoolean.class.cast(o).setValue(Boolean.valueOf(value));
		} else if (DEFInteger.class.isInstance(o)) {
			DEFInteger.class.cast(o).setValue(Integer.valueOf(value));
		} else if (DEFDouble.class.isInstance(o)) {
			DEFDouble.class.cast(o).setValue(Double.valueOf(value));
		}
		return String.format(MESSAGE_OBJECT_UPDATED, name);
	}


	@CliCommand(value = CMD_OBJECT_UPDATE_RESOURCE, help = "Update the given Resource object.")
	public String updateResource(
		@CliOption(key = OPT_NAME, mandatory = true, help = "Name of resource") final String name,
		@CliOption(key = OPT_DATA, mandatory = true, help = "Data object") final String data

	) throws TException {
		ResourceDTO resource = getObject(name, ResourceDTO.class);
		if (data != null) {
			TSerializer serializer = new TSerializer();
			resource.setData(serializer.serialize(getObject(data, TBase.class)));
		}
		return String.format(MESSAGE_OBJECT_UPDATED, name);
	}

	/**
	 * Returns the requested object (casted).
	 *
	 * @param name - name of object
	 * @param objectType - type
	 * @param <T> - type
	 * @return casted object
	 */
	public <T> T getObject(String name, Class<T> objectType) {
		if (!objectMap.containsKey(name)) {
			throw new UnknownObjectException(String.format(MESSAGE_OBJECT_NOT_EXISTS, name));
		}
		try {
			return objectType.cast(objectMap.get(name));
		} catch (ClassCastException e) {
			throw new WrongTypeException(String.format(MESSAGE_OBJECT_WRONG_TYPE, name, objectType), e);
		}
	}


	Map<String, Object> getObjectMap() {
		return objectMap;
	}
}
