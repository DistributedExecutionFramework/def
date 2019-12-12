package at.enfilo.def.cloud.communication.logic.specific.aws;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all the data that is necessary for the communication with AWS
 */
public class AWSSpecification extends CloudSpecification {

    private static final String CLUSTER_CLOUD_INIT_SCRIPT_FILE = "clusterCloudInitScript.txt";
    private static final String WORKER_CLOUD_INIT_SCRIPT_FILE = "workerCloudInitScript.txt";
    private static final String REDUCER_CLOUD_INIT_SCRIPT_FILE = "reducerCloudInitScript.txt";
    private static final int TIMEOUT_FOR_INSTANCE_BOOTING_IN_SECONDS = 60;

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(CloudSpecification.class);

    private Map<InstanceType, String> cloudInitScriptsMap;

    public AWSSpecification() {
        this.cloudInitScriptsMap = new HashMap<>();
        this.cloudInitScriptsMap.put(InstanceType.CLUSTER, prepareCloudInitScript(CLUSTER_CLOUD_INIT_SCRIPT_FILE));
        this.cloudInitScriptsMap.put(InstanceType.WORKER, prepareCloudInitScript(WORKER_CLOUD_INIT_SCRIPT_FILE));
        this.cloudInitScriptsMap.put(InstanceType.REDUCER, prepareCloudInitScript(REDUCER_CLOUD_INIT_SCRIPT_FILE));
    }


    public AWSSpecification(AWSSpecificationDTO specificationDTO) {
        this();

        setInstanceTypeSpecification(InstanceType.CLUSTER, specificationDTO.getClusterImageId(), specificationDTO.getClusterInstanceSize());
        setInstanceTypeSpecification(InstanceType.WORKER, specificationDTO.getWorkerImageId(), specificationDTO.getWorkerInstanceSize());
        setInstanceTypeSpecification(InstanceType.REDUCER, specificationDTO.getReducerImageId(), specificationDTO.getReducerInstanceSize());
        this.accessKeyID = specificationDTO.getAccessKeyID();
        this.secretKey = specificationDTO.getSecretKey();
        this.region = specificationDTO.getRegion();
        this.publicSubnetId = specificationDTO.getPublicSubnetId();
        this.privateSubnetId = specificationDTO.getPrivateSubnetId();
        this.vpcId = specificationDTO.getVpcId();
        this.keypairName = specificationDTO.getKeypairName();
        this.vpnDynamicIpNetworkAddress = specificationDTO.getVpnDynamicIpNetworkAddress();
        this.vpnDynamicIpSubnetMaskSuffix = specificationDTO.getVpnDynamicIpSubnetMaskSuffix();
    }

    protected String accessKeyID;
    protected String secretKey;
    protected String region;
    protected String publicSubnetId;
    protected String privateSubnetId;
    protected String vpcId;
    protected String keypairName;
    protected String vpnDynamicIpNetworkAddress;
    protected int vpnDynamicIpSubnetMaskSuffix;

    @Override
    public boolean isCloudSpecificationComplete() {
        if (this.accessKeyID == null) {
            return false;
        }
        if (this.secretKey == null) {
            return false;
        }
        if (this.region == null) {
            return false;
        }
        if (this.publicSubnetId == null) {
            return false;
        }
        if (this.privateSubnetId == null) {
            return false;
        }
        if (this.vpcId == null) {
            return false;
        }
        if (this.keypairName == null) {
            return false;
        }
        if (this.vpnDynamicIpNetworkAddress == null) {
            return false;
        }
        if (this.vpnDynamicIpSubnetMaskSuffix == 0) {
            return false;
        }
        if (this.imageIds.size() != InstanceType.values().length) {
            return false;
        }
        if (this.instanceSizes.size() != InstanceType.values().length) {
            return false;
        }
        return true;
    }

    @Override
    public int getTimeoutForInstanceBootingInSeconds() {
        return TIMEOUT_FOR_INSTANCE_BOOTING_IN_SECONDS;
    }

    public String getAccessKeyID() {
        return accessKeyID;
    }

    public void setAccessKeyID(String accessKeyID) {
        this.accessKeyID = accessKeyID;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPublicSubnetId() {
        return publicSubnetId;
    }

    public void setPublicSubnetId(String publicClusterSubnetId) {
        this.publicSubnetId = publicClusterSubnetId;
    }

    public String getPrivateSubnetId() {
        return privateSubnetId;
    }

    public void setPrivateSubnetId(String privateWorkerSubnetId) {
        this.privateSubnetId = privateWorkerSubnetId;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getKeypairName() {
        return keypairName;
    }

    public void setKeypairName(String keypairName) {
        this.keypairName = keypairName;
    }

    public String getVpnDynamicIpNetworkAddress() {
        return vpnDynamicIpNetworkAddress;
    }

    public void setVpnDynamicIpNetworkAddress(String vpnDynamicIpNetworkAddress) {
        this.vpnDynamicIpNetworkAddress = vpnDynamicIpNetworkAddress;
    }

    public int getVpnDynamicIpSubnetMaskSuffix() {
        return vpnDynamicIpSubnetMaskSuffix;
    }

    public void setVpnDynamicIpSubnetMaskSuffix(int vpnDynamicIpSubnetMaskSuffix) {
        this.vpnDynamicIpSubnetMaskSuffix = vpnDynamicIpSubnetMaskSuffix;
    }

    public String getCloudInitScript(InstanceType instanceType) {
        if (!this.cloudInitScriptsMap.containsKey(instanceType)) {
            throw new IllegalArgumentException(MessageFormat.format("There is no cloud init script for instance type {0} set.", instanceType));
        }
        return this.cloudInitScriptsMap.get(instanceType);
    }

    private String prepareCloudInitScript(String cloudInitScriptFileName) {
        try {
            return readFromFile(cloudInitScriptFileName);
        } catch (FileNotFoundException e) {
            LOGGER.error(MessageFormat.format("File {0} could not be found.", cloudInitScriptFileName));
        }
        return null;
    }

    private String readFromFile(String fileName) throws FileNotFoundException {
        LOGGER.info(MessageFormat.format("Trying to read from file with name {0}", fileName));
        StringBuilder fileContent = new StringBuilder();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream fileInputStream = classLoader.getResourceAsStream(fileName);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = br.readLine()) != null) {
                fileContent.append(line);
                fileContent.append("\n");
            }
        } catch (IOException e) {
            LOGGER.error(MessageFormat.format("Error while reading file {0}.", fileName), e);
        }
        LOGGER.debug(MessageFormat.format("Successfully read content from file {0}", fileName));
        return fileContent.toString();
    }
}
