package at.enfilo.def.cloud.communication.logic.specific.aws;

import at.enfilo.def.cloud.communication.logic.general.CloudCluster;
import at.enfilo.def.cloud.communication.logic.general.CloudInstance;
import at.enfilo.def.cloud.communication.logic.general.CloudSpecification;
import at.enfilo.def.cloud.communication.logic.general.ICloudFactory;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.text.MessageFormat;

/**
 * Implements the {@link ICloudFactory} interface and is responsible for creating the AWS specific objects for the methods of the {@link ICloudFactory}
 */
public class AWSFactory implements ICloudFactory {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(AWSFactory.class);

    /**
     * Creates an object of type {@link AWSInstance}
     *
     * @param type                  the {@link InstanceType} the newly created {@link CloudInstance} will have
     * @param cloudSpecification    the specification data of type {@link CloudSpecification} the created {@link CloudInstance} needs for connecting to a specific cloud environment
     * @param cloudCluster          the {@link CloudCluster} the {@link CloudInstance} shall be created in
     * @return                       a newly created {@link AWSInstance} with the given {@link InstanceType} and {@link AWSSpecification} set
     */
    @Override
    public CloudInstance createCloudInstance(InstanceType type, CloudSpecification cloudSpecification, CloudCluster cloudCluster) {
        LOGGER.info(MessageFormat.format("Creating AWS instance of type {0}", type));
        if (!(cloudCluster instanceof AWSCluster)) {
            throw new IllegalArgumentException("AWSCloudFactory needs a cloud cluster of type AWSCluster");
        }
        AWSCluster awsCluster = (AWSCluster)cloudCluster;
        return new AWSInstance(type, cloudSpecification, awsCluster.getNetworkInterfaceSpecifications(type), awsCluster.getAwsClient());
    }

    /**
     * Creates an object of type {@link AWSSpecification}
     *
     * @return  an empty object of type {@link AWSSpecification}
     */
    @Override
    public CloudSpecification createCloudSpecification() {
        LOGGER.info("Creating AWS specification");
        return new AWSSpecification();
    }

    /**
     * Creates an object of type {@link AWSCluster}
     *
     * @param cloudFactory          the {@link ICloudFactory} itself that handles the creation of the cloud specific objects in the {@link CloudCluster}
     * @param cloudSpecification    the specification data of type {@link CloudSpecification} the created {@link CloudCluster} needs for connecting to a specific cloud environment
     * @return                      the newly created {@link AWSInstance} with the given {@link AWSSpecification} set
     */
    @Override
    public CloudCluster createCloudCluster(ICloudFactory cloudFactory, CloudSpecification cloudSpecification) {
        LOGGER.info("Creating AWS cluster");
        return new AWSCluster(cloudFactory, cloudSpecification);
    }
}
