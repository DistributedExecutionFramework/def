package at.enfilo.def.cloud.communication.logic.general;

import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the IDs of the images that shall be booted in the cloud and
 * the corresponding instance size for each {@link InstanceType}
 */
public abstract class CloudSpecification {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(CloudSpecification.class);

    protected Map<InstanceType, String> imageIds;
    protected Map<InstanceType, String> instanceSizes;

    public CloudSpecification() {
        this.imageIds = new HashMap<>();
        this.instanceSizes = new HashMap<>();
        LOGGER.debug("New CloudSpecification created");
    }

    /**
     * Creates a new specification entry for the given {@link InstanceType} with the given image ID and instance size set
     *
     * @param instanceType  the {@link InstanceType} the specification entry is for
     * @param imageId       the ID of the image that shall be booted for the given {@link InstanceType}
     * @param instanceSize  the size the booted instances of this {@link InstanceType} shall have
     */
    public void setInstanceTypeSpecification(InstanceType instanceType, String imageId, String instanceSize) {
        LOGGER.info(MessageFormat.format("Setting new specification entry for type {0} with image id {1} and instance size {2}", instanceType, imageId, instanceSize));
        this.imageIds.put(instanceType, imageId);
        this.instanceSizes.put(instanceType, instanceSize);
    }

    /**
     * Looks up the ID of the image that shall be booted in the cloud for the given {@link InstanceType}
     *
     * @param instanceType              the {@link InstanceType} the image ID should be returned of
     * @return                          the ID of the image that shall be booted for the given {@link InstanceType}
     * @throws IllegalStateException    if there is no image ID set for the given {@link InstanceType}
     */
    public String getImageId(InstanceType instanceType) {
        LOGGER.info(MessageFormat.format("Fetching image id for type {0}", instanceType));
        if (!this.imageIds.containsKey(instanceType)) {
            throw new IllegalStateException("There is no image id set for this instance type.");
        }
        return this.imageIds.get(instanceType);
    }

    /**
     * Looks up the size of the instance that shall be booted in the cloud for the given {@link InstanceType}
     *
     * @param instanceType              the {@link InstanceType} the instance size should be returned of
     * @return                          the size of the instance that shall be booted for the given {@link InstanceType}
     * @throws IllegalStateException    if there is no instance size set for the given {@link InstanceType}
     */
    public String getInstanceSize(InstanceType instanceType) {
        LOGGER.info(MessageFormat.format("Fetching instance size for type {0}", instanceType));
        if (!this.instanceSizes.containsKey(instanceType)) {
            throw new IllegalStateException("There is no instance size set for this instance type.");
        }
        return this.instanceSizes.get(instanceType);
    }

    /**
     * Checks if all necessary data is set in the cloud specification
     *
     * @return  true, if all necessary data is set, otherwise false
     */
    public abstract boolean isCloudSpecificationComplete();

    public abstract int getTimeoutForInstanceBootingInSeconds();
}
