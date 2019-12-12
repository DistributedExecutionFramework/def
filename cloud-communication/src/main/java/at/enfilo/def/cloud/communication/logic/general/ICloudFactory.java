package at.enfilo.def.cloud.communication.logic.general;

import at.enfilo.def.cloud.communication.logic.util.InstanceType;

/**
 * The ICloudFactory is responsible for creating all objects that are necessary for the communication with a specific cloud
 */
public interface ICloudFactory {

    /**
     * Creates an object of type {@link CloudInstance}
     *
     * @param type                  the {@link InstanceType} the newly created {@link CloudInstance} will have
     * @param cloudSpecification    the specification data of type {@link CloudSpecification} the created {@link CloudInstance} needs for connecting to a specific cloud environment
     * @param cloudCluster          the {@link CloudCluster} the {@link CloudInstance} shall be created in
     * @return                      a newly created {@link CloudInstance} with the given {@link InstanceType} and {@link CloudSpecification} set
     */
    CloudInstance createCloudInstance(InstanceType type, CloudSpecification cloudSpecification, CloudCluster cloudCluster);

    /**
     * Creates an object of type {@link CloudSpecification}
     *
     * @return  an empty object of type {@link CloudSpecification}
     */
    CloudSpecification createCloudSpecification();

    /**
     * Creates an object of type {@link CloudCluster}
     *
     * @param cloudFactory          the {@link ICloudFactory} itself that handles the creation of the cloud specific objects in the {@link CloudCluster}
     * @param cloudSpecification    the specification data of type {@link CloudSpecification} the created {@link CloudCluster} needs for connecting to a specific cloud environment
     * @return                      the newly created {@link CloudCluster} with the given {@link CloudSpecification} set
     */
    CloudCluster createCloudCluster(ICloudFactory cloudFactory, CloudSpecification cloudSpecification);

}
