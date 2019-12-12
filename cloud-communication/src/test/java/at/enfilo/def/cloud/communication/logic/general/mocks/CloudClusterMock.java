package at.enfilo.def.cloud.communication.logic.general.mocks;

import at.enfilo.def.cloud.communication.logic.general.CloudCluster;
import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.general.ICloudFactory;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;

import java.util.Collection;
import java.util.Map;

public class CloudClusterMock extends CloudCluster {

    public int counterApplyCloudSettingsForBootingCloudInstanceCalls = 0;
    public int counterTerminateNodeCloudInstancesCalls = 0;
    private boolean useTerminationCheckForCloudInstance;

    /**
     * Creates a {@link CloudCluster} object with the given {@link ICloudFactory} and the given {@link CloudSpecification}
     *
     * @param cloudFactory       the {@link ICloudFactory} this {@link CloudCluster} uses for creating the appropriate objects for
     *                           the communication with the cloud environment
     * @param cloudSpecification the specification data needed for the communication with the cloud environment as {@link CloudSpecification}
     */
    public CloudClusterMock(ICloudFactory cloudFactory, CloudSpecification cloudSpecification, boolean useTerminationCheckForCloudInstance) {
        super(cloudFactory, cloudSpecification);

        this.useTerminationCheckForCloudInstance = useTerminationCheckForCloudInstance;
    }

    @Override
    protected void initialize() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    protected void applyCloudSettingsForBootingCloudInstance(CloudInstance cloudInstance) {
        this.counterApplyCloudSettingsForBootingCloudInstanceCalls++;
    }

    @Override
    public boolean canCloudInstanceBeTerminated(CloudInstance cloudInstance) {
        if (useTerminationCheckForCloudInstance) {
            return super.canCloudInstanceBeTerminated(cloudInstance);
        }
        return true;
    }

    public CloudInstance getClusterInstanceSet() {
        return this.clusterInstance;
    }

    public void setClusterInstance(CloudInstance clusterInstance) {
        this.clusterInstance = clusterInstance;
    }

    public Map<String, CloudInstance> getNodeInstancesMapSet() {
        return this.nodeInstancesMap;
    }

    public void setNodeInstancesMap(Map<String, CloudInstance> map) {
        this.nodeInstancesMap = map;
    }

    public Map<String, InstanceType> getRemovedInstanceIdsMapSet() {
        return this.removedInstanceIds;
    }

    public CloudSpecification getCloudSpecificationSet() {
        return this.cloudSpecification;
    }

    public ICloudFactory getCloudFactorySet() {
        return this.cloudFactory;
    }

    @Override
    public CloudInstance getCloudInstance(String cloudInstanceId) {
        return super.getCloudInstance(cloudInstanceId);
    }

    public Map<String, String> getDEFIdsToCloudIsMap() {
        return this.defIdsToCloudIdsMap;
    }

    public String getCloudClusterIdSet() {
        return this.cloudClusterId;
    }

    public void setCloudClusterId(String cloudClusterId) {
        this.cloudClusterId = cloudClusterId;
    }

    @Override
    public String getCloudInstanceId(String defInstanceId) {
        return super.getCloudInstanceId(defInstanceId);
    }

    @Override
    public boolean isCloudInstanceWithIdRegistered(String cloudInstanceId) {
        return super.isCloudInstanceWithIdRegistered(cloudInstanceId);
    }

    @Override
    public void terminateNodeCloudInstances(Collection<String> cloudInstanceIds) {
        this.counterTerminateNodeCloudInstancesCalls++;
        super.terminateNodeCloudInstances(cloudInstanceIds);
    }
}
