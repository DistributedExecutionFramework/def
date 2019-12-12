package at.enfilo.def.client.shell;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Test;
import org.springframework.shell.core.CommandResult;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import static at.enfilo.def.client.shell.Constants.*;
import static org.junit.Assert.*;

public class ObjectCommandsTest extends ShellBaseTest {

	@Test
	public void createObject() throws Exception {
		String name = UUID.randomUUID().toString();
		ObjectCommands.ObjectType type = ObjectCommands.ObjectType.SERVICE_ENDPOINT;

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_CREATE,
						OPT_TYPE, type,
						OPT_NAME, name)
		);
		assertTrue(result.isSuccess());
		assertEquals(
			String.format(MESSAGE_OBJECT_CREATED, type, name),
			result.getResult().toString()
		);
		assertTrue(objects.getObjectMap().containsKey(name));

		// Create again the same object
		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_CREATE,
						OPT_TYPE, type,
						OPT_NAME, name)
		);
		assertFalse(result.isSuccess());
		assertEquals(String.format(MESSAGE_OBJECT_ALREADY_EXISTS, name), result.getException().getMessage());


		// Create all other object types
		for (ObjectCommands.ObjectType t : ObjectCommands.ObjectType.values()) {
			name = UUID.randomUUID().toString();
			result = shell.executeCommand(
					String.format("%s --%s %s --%s %s", CMD_OBJECT_CREATE,
							OPT_TYPE, t,
							OPT_NAME, name)
			);
			assertTrue(result.isSuccess());
			switch (t) {
				case TASK:
					assertEquals(TaskDTO.class, objects.getObjectMap().get(name).getClass());
					break;
				case JOB:
					assertEquals(JobDTO.class, objects.getObjectMap().get(name).getClass());
					break;
				case PROGRAM:
					assertEquals(ProgramDTO.class, objects.getObjectMap().get(name).getClass());
					break;
				case RESOURCE:
					assertEquals(ResourceDTO.class, objects.getObjectMap().get(name).getClass());
					break;
				case SERVICE_ENDPOINT:
					assertEquals(ServiceEndpointDTO.class, objects.getObjectMap().get(name).getClass());
					break;
			}
		}
	}


	@Test
	public void listObjects() throws Exception {
		String n1 = "name1";
		String n2 = "name2";
		objects.getObjectMap().put(n1, new String());
		objects.getObjectMap().put(n2, new Integer(1));

		CommandResult result = shell.executeCommand(CMD_OBJECT_LIST);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(
					String.format("%s: %s", n1, String.class.getSimpleName())
				));
		assertTrue(result.getResult().toString().contains(
				String.format("%s: %s", n2, Integer.class.getSimpleName())
		));
	}

	@Test
	public void showObject() throws Exception {
		String name = "i1";
		Integer value = new Integer(123456);
		objects.getObjectMap().put(name, value);

		CommandResult result = shell.executeCommand(String.format("%s --%s %s", CMD_OBJECT_SHOW, OPT_NAME, name));
		assertTrue(result.isSuccess());
		assertEquals(value.toString(), result.getResult().toString());

		String name2 = UUID.randomUUID().toString();
		result = shell.executeCommand(String.format("%s --%s %s", CMD_OBJECT_SHOW, OPT_NAME, name2));
		assertFalse(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_OBJECT_NOT_EXISTS, name2),
				result.getException().getMessage()
		);
	}

	@Test
	public void deleteObject() throws Exception {
		String name = "d1";
		Double value = new Double(123.456);
		objects.getObjectMap().put(name, value);

		CommandResult result = shell.executeCommand(String.format("%s --%s %s", CMD_OBJECT_REMOVE, OPT_NAME, name));
		assertTrue(result.isSuccess());
		assertEquals(String.format(MESSAGE_OBJECT_REMOVE, name), result.getResult().toString());
		assertFalse(objects.getObjectMap().containsKey(name));

		String name2 = UUID.randomUUID().toString();
		result = shell.executeCommand(String.format("%s --%s %s", CMD_OBJECT_REMOVE, OPT_NAME, name2));
		assertFalse(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_OBJECT_NOT_EXISTS, name2),
				result.getException().getMessage()
		);
	}


	@Test
	public void updateServiceEndpoint() throws Exception {
		String name = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		objects.getObjectMap().put(name, endpoint);

		String host = UUID.randomUUID().toString();
		int port = new Random().nextInt();
		Protocol protocol = Protocol.REST;
		String pattern = UUID.randomUUID().toString();

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_ENDPOINT,
						OPT_NAME, name,
						OPT_HOST, host
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(host, endpoint.getHost());
		assertNotEquals(port, endpoint.getPort());
		assertNotEquals(protocol, endpoint.getProtocol());
		assertNotEquals(pattern, endpoint.getPathPrefix());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_ENDPOINT,
						OPT_NAME, name,
						OPT_PORT, port
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(host, endpoint.getHost());
		assertEquals(port, endpoint.getPort());
		assertNotEquals(protocol, endpoint.getProtocol());
		assertNotEquals(pattern, endpoint.getPathPrefix());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_ENDPOINT,
						OPT_NAME, name,
						OPT_PROTOCOL, protocol
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(host, endpoint.getHost());
		assertEquals(port, endpoint.getPort());
		assertEquals(protocol, endpoint.getProtocol());
		assertNotEquals(pattern, endpoint.getPathPrefix());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_ENDPOINT,
						OPT_NAME, name,
						OPT_URL_PATTERN, pattern
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(host, endpoint.getHost());
		assertEquals(port, endpoint.getPort());
		assertEquals(protocol, endpoint.getProtocol());
		assertEquals(pattern, endpoint.getPathPrefix());
	}


	@Test
	public void getObject() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();
		objects.getObjectMap().put("se", se);

		assertEquals(se, objects.getObject("se", ServiceEndpointDTO.class));
	}

	@Test(expected = UnknownObjectException.class)
	public void getUnknownObject() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();
		objects.getObjectMap().put("se", se);

		assertEquals(se, objects.getObject(UUID.randomUUID().toString(), ServiceEndpointDTO.class));
	}

	@Test(expected = WrongTypeException.class)
	public void getWrongObject() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();
		objects.getObjectMap().put("se", se);

		assertEquals(se, objects.getObject("se", TaskDTO.class));
	}

	@Test
	public void updateDEFScalar() throws Exception {
		objects.getObjectMap().put("dbl", new DEFDouble());
		objects.getObjectMap().put("int", new DEFInteger());

		double dbl = new Random().nextDouble();
		CommandResult result = shell.executeCommand(
				String.format(Locale.ROOT, "%s --%s %s --%s %f", CMD_OBJECT_UPDATE_DEF_DOUBLE,
						OPT_NAME, "dbl",
						OPT_VALUE, dbl
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(dbl, objects.getObject("dbl", DEFDouble.class).getValue(), 0.00001);

		int i = new Random().nextInt();
		result = shell.executeCommand(
				String.format("%s --%s %s --%s %d", CMD_OBJECT_UPDATE_DEF_INTEGER,
						OPT_NAME, "int",
						OPT_VALUE, i
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(i, objects.getObject("int", DEFInteger.class).getValue());
	}

	@Test
	public void updateAWSspecificationTest() throws Exception {
		String name = UUID.randomUUID().toString();
		AWSSpecificationDTO specification = new AWSSpecificationDTO();
		objects.getObjectMap().put(name, specification);

		String accessKeyId = UUID.randomUUID().toString();
		String secretKey = UUID.randomUUID().toString();
		String region = "europe";
		String publicSubnetId = UUID.randomUUID().toString();
		String privateSubnetId = UUID.randomUUID().toString();
		String vpdIc = UUID.randomUUID().toString();
		String keypairName = "keypair";
		String vpnDynamicIpNetworkAddress = "127.0.0.1";
		String vpnDynamicIpSubnetMask = "20";
		String clusterImageId = UUID.randomUUID().toString();
		String clusterInstanceSize = "huge";
		String workerImageId = UUID.randomUUID().toString();
		String workerInstanceSize = "huge";
		String reducerImageId = UUID.randomUUID().toString();
		String reducerInstanceSize = "huge";

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_AWS_ACCESS_KEY_ID, accessKeyId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertNotEquals(secretKey, specification.getSecretKey());
		assertNotEquals(region, specification.getRegion());
		assertNotEquals(publicSubnetId, specification.getPublicSubnetId());
		assertNotEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertNotEquals(vpdIc, specification.getVpcId());
		assertNotEquals(keypairName, specification.getKeypairName());
		assertNotEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertNotEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_AWS_SECRET_KEY, secretKey
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertNotEquals(region, specification.getRegion());
		assertNotEquals(publicSubnetId, specification.getPublicSubnetId());
		assertNotEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertNotEquals(vpdIc, specification.getVpcId());
		assertNotEquals(keypairName, specification.getKeypairName());
		assertNotEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertNotEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_AWS_REGION, region
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertNotEquals(publicSubnetId, specification.getPublicSubnetId());
		assertNotEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertNotEquals(vpdIc, specification.getVpcId());
		assertNotEquals(keypairName, specification.getKeypairName());
		assertNotEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertNotEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_AWS_PUBLIC_SUBNET_ID, publicSubnetId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertNotEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertNotEquals(vpdIc, specification.getVpcId());
		assertNotEquals(keypairName, specification.getKeypairName());
		assertNotEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertNotEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_AWS_PRIVATE_SUBNET_ID, privateSubnetId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertNotEquals(vpdIc, specification.getVpcId());
		assertNotEquals(keypairName, specification.getKeypairName());
		assertNotEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertNotEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_AWS_VPC_ID, vpdIc
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertNotEquals(keypairName, specification.getKeypairName());
		assertNotEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertNotEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_AWS_KEYPAIR_NAME, keypairName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertNotEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertNotEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_VPN_DYNAMIC_IP_NETWORK_ADDRESS, vpnDynamicIpNetworkAddress
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertNotEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_VPN_DYNAMIC_IP_SUBNET_MASK, vpnDynamicIpSubnetMask
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertNotEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_CLUSTER_IMAGE_ID, clusterImageId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertEquals(clusterImageId, specification.getClusterImageId());
		assertNotEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_CLUSTER_INSTANCE_SIZE, clusterInstanceSize
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertEquals(clusterImageId, specification.getClusterImageId());
		assertEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertNotEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_WORKER_IMAGE_ID, workerImageId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertEquals(clusterImageId, specification.getClusterImageId());
		assertEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertEquals(workerImageId, specification.getWorkerImageId());
		assertNotEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_WORKER_INSTANCE_SIZE, workerInstanceSize
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertEquals(clusterImageId, specification.getClusterImageId());
		assertEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertEquals(workerImageId, specification.getWorkerImageId());
		assertEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertNotEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_REDUCER_IMAGE_ID, reducerImageId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertEquals(clusterImageId, specification.getClusterImageId());
		assertEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertEquals(workerImageId, specification.getWorkerImageId());
		assertEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertEquals(reducerImageId, specification.getReducerImageId());
		assertNotEquals(reducerInstanceSize, specification.getReducerInstanceSize());

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_OBJECT_UPDATE_AWS_SPECIFICATION,
						OPT_NAME, name,
						OPT_REDUCER_INSTANCE_SIZE, reducerInstanceSize
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(accessKeyId, specification.getAccessKeyID());
		assertEquals(secretKey, specification.getSecretKey());
		assertEquals(region, specification.getRegion());
		assertEquals(publicSubnetId, specification.getPublicSubnetId());
		assertEquals(privateSubnetId, specification.getPrivateSubnetId());
		assertEquals(vpdIc, specification.getVpcId());
		assertEquals(keypairName, specification.getKeypairName());
		assertEquals(vpnDynamicIpNetworkAddress, specification.getVpnDynamicIpNetworkAddress());
		assertEquals(Integer.parseInt(vpnDynamicIpSubnetMask), specification.getVpnDynamicIpSubnetMaskSuffix());
		assertEquals(clusterImageId, specification.getClusterImageId());
		assertEquals(clusterInstanceSize, specification.getClusterInstanceSize());
		assertEquals(workerImageId, specification.getWorkerImageId());
		assertEquals(workerInstanceSize, specification.getWorkerInstanceSize());
		assertEquals(reducerImageId, specification.getReducerImageId());
		assertEquals(reducerInstanceSize, specification.getReducerInstanceSize());
	}
}
