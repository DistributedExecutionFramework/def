package at.enfilo.def.cloud.communication.logic.general;

import at.enfilo.def.cloud.communication.logic.general.mocks.CloudSpecificationMock;
import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CloudSpecificationTest {

    private CloudSpecification cloudSpecification;
    private InstanceType instanceType;
    private String imageId;
    private String instanceSize;
    private Map<InstanceType, String> imageIdsMap;
    private Map<InstanceType, String> instanceSizesMap;

    @Before
    public void initialize() {
        this.cloudSpecification = new CloudSpecificationMock();
        this.instanceType = InstanceType.REDUCER;
        this.imageId = "1234";
        this.instanceSize = "huge";

        this.imageIdsMap = new HashMap<>();
        this.instanceSizesMap = new HashMap<>();
    }

    @Test
    public void CloudSpecificationConstructorTest() {
        CloudSpecificationMock specification = new CloudSpecificationMock();

        Assert.assertNotNull(specification.getImageIdsMap());
        Assert.assertNotNull(specification.getInstanceSizesMap());
    }

    @Test
    public void setInstanceTypeSpecificationTest() {
        CloudSpecificationMock specification = new CloudSpecificationMock();
        int imageIdsMapSize = specification.getImageIdsMap().size();
        int instanceSizesMapSize = specification.getInstanceSizesMap().size();
        Assert.assertFalse(specification.getImageIdsMap().containsKey(this.instanceType));
        Assert.assertFalse(specification.getInstanceSizesMap().containsKey(this.instanceType));

        specification.setInstanceTypeSpecification(this.instanceType, this.imageId, this.instanceSize);

        Assert.assertEquals(imageIdsMapSize + 1, specification.getImageIdsMap().size());
        Assert.assertEquals(instanceSizesMapSize + 1, specification.getInstanceSizesMap().size());
        Assert.assertTrue(specification.getImageIdsMap().containsKey(this.instanceType));
        Assert.assertTrue(specification.getInstanceSizesMap().containsKey(this.instanceType));
        Assert.assertEquals(this.imageId, specification.getImageIdsMap().get(this.instanceType));
        Assert.assertEquals(this.instanceSize, specification.getInstanceSizesMap().get(this.instanceType));
    }

    @Test
    public void getImageIdTest_instanceTypeContained() {
        this.imageIdsMap.put(this.instanceType, this.imageId);
        CloudSpecificationMock specification = new CloudSpecificationMock();
        specification.setImageIdsMap(this.imageIdsMap);

        String imageId = specification.getImageId(this.instanceType);

        Assert.assertEquals(this.imageId, imageId);
    }

    @Test (expected = IllegalStateException.class)
    public void getImageIdTest_instanceTypeNotContained() {
        CloudSpecificationMock specification = new CloudSpecificationMock();
        specification.setImageIdsMap(this.imageIdsMap);

        specification.getImageId(this.instanceType);
    }

    @Test
    public void getInstanceSize_instanceTypeContained() {
        this.instanceSizesMap.put(this.instanceType, this.instanceSize);
        CloudSpecificationMock specification = new CloudSpecificationMock();
        specification.setInstanceSizesMap(this.instanceSizesMap);

        String instancesSize = specification.getInstanceSize(this.instanceType);

        Assert.assertEquals(this.instanceSize, instancesSize);
    }

    @Test (expected = IllegalStateException.class)
    public void getInstanceSize_instanceTypeNotContained() {
        CloudSpecificationMock specification = new CloudSpecificationMock();
        specification.setInstanceSizesMap(this.instanceSizesMap);

        specification.getInstanceSize(this.instanceType);
    }
}
