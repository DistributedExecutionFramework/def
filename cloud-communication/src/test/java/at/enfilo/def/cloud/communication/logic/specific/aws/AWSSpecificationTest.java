package at.enfilo.def.cloud.communication.logic.specific.aws;

import at.enfilo.def.cloud.communication.logic.util.InstanceType;
import org.junit.Assert;
import org.junit.Test;

public class AWSSpecificationTest extends AWSSpecification {

    private void prepareCompleteCloudSpecification() {
        this.accessKeyID = "12345";
        this.secretKey = "12345";
        this.region = "europe";
        this.publicSubnetId = "12345";
        this.privateSubnetId = "12345";
        this.vpcId = "12345";
        this.keypairName = "keypair";
        this.vpnDynamicIpNetworkAddress = "0.0.0.0";
        this.vpnDynamicIpSubnetMaskSuffix = 20;
        setInstanceTypeSpecification(InstanceType.WORKER, "1234", "huge");
        setInstanceTypeSpecification(InstanceType.REDUCER, "1234", "huge");
        setInstanceTypeSpecification(InstanceType.CLUSTER, "1234", "huge");
    }

    @Test
    public void isCloudSpecificationCompleteTest_isComplete() {
        prepareCompleteCloudSpecification();

        Assert.assertTrue(isCloudSpecificationComplete());
    }

    @Test
    public void isCloudSpecificationCompleteTest_isNotComplete() {
        prepareCompleteCloudSpecification();
        this.accessKeyID = null;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.secretKey = null;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.region = null;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.publicSubnetId = null;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.privateSubnetId = null;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.vpcId = null;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.keypairName = null;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.vpnDynamicIpNetworkAddress = null;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.vpnDynamicIpSubnetMaskSuffix = 0;
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.imageIds.clear();
        Assert.assertFalse(isCloudSpecificationComplete());

        prepareCompleteCloudSpecification();
        this.instanceSizes.clear();
        Assert.assertFalse(isCloudSpecificationComplete());
    }
    
    @Test
    public void getAccessKeyIDTest() {
        String accessKeyID = "1234567";
        Assert.assertNull(this.accessKeyID);

        this.accessKeyID = accessKeyID;
        Assert.assertEquals(accessKeyID, getAccessKeyID());
    }

    @Test
    public void setAccessKeyIDTest() {
        String accessKeyID = "1234567";
        Assert.assertNull(this.accessKeyID);

        setAccessKeyID(accessKeyID);
        Assert.assertEquals(accessKeyID, this.accessKeyID);
    }

    @Test
    public void getSecretKeyTest() {
        String secretKey = "ThisIsASecretKey";
        Assert.assertNull(this.secretKey);

        this.secretKey = secretKey;
        Assert.assertEquals(secretKey, getSecretKey());
    }

    @Test
    public void setSecretKeyTest() {
        String secretKey = "ThisIsASecretKey";
        Assert.assertNull(this.secretKey);

        setSecretKey(secretKey);
        Assert.assertEquals(secretKey, this.secretKey);
    }

    @Test
    public void getRegionTest() {
        String region = "Austria";
        Assert.assertNull(this.region);

        this.region = region;
        Assert.assertEquals(region, getRegion());
    }

    @Test
    public void setRegionTest() {
        String region = "Austria";
        Assert.assertNull(this.region);

        setRegion(region);
        Assert.assertEquals(region, this.region);
    }

    @Test
    public void getPublicSubnetIdTest() {
        String publicSubnetId = "1234";
        Assert.assertNull(this.publicSubnetId);

        this.publicSubnetId = publicSubnetId;
        Assert.assertEquals(publicSubnetId, getPublicSubnetId());
    }

    @Test
    public void setPublicSubnetIdTest() {
        String publicSubnetId = "1234";
        Assert.assertNull(this.publicSubnetId);

        setPublicSubnetId(publicSubnetId);
        Assert.assertEquals(publicSubnetId, this.publicSubnetId);
    }

    @Test
    public void getPrivateSubnetIdTest() {
        String privateSubnetId = "2345";
        Assert.assertNull(this.privateSubnetId);

        this.privateSubnetId = privateSubnetId;
        Assert.assertEquals(privateSubnetId, getPrivateSubnetId());
    }

    @Test
    public void setPrivateSubnetIdTest() {
        String privateSubnetId = "2345";
        Assert.assertNull(this.privateSubnetId);

        setPrivateSubnetId(privateSubnetId);
        Assert.assertEquals(privateSubnetId, this.privateSubnetId);
    }

    @Test
    public void getVpcIdTest() {
        String vpcId = "3456";
        Assert.assertNull(this.vpcId);

        this.vpcId = vpcId;
        Assert.assertEquals(vpcId, getVpcId());
    }

    @Test
    public void setVpcIdTest() {
        String vpcId = "3456";
        Assert.assertNull(this.vpcId);

        setVpcId(vpcId);
        Assert.assertEquals(vpcId, this.vpcId);
    }

    @Test
    public void getKeypairNameTest() {
        String keypairName = "KeyPairOne";
        Assert.assertNull(this.keypairName);

        this.keypairName = keypairName;
        Assert.assertEquals(keypairName, getKeypairName());
    }

    @Test
    public void setKeypairNameTest() {
        String keypairName = "KeyPairOne";
        Assert.assertNull(this.keypairName);

        setKeypairName(keypairName);
        Assert.assertEquals(keypairName, this.keypairName);
    }

    @Test
    public void getVpnDynamicIpNetworkAddressTest() {
        String vpnDynamicIpNetworkAddress = "1.1.1.1";
        Assert.assertNull(this.vpnDynamicIpNetworkAddress);

        this.vpnDynamicIpNetworkAddress = vpnDynamicIpNetworkAddress;
        Assert.assertEquals(vpnDynamicIpNetworkAddress, getVpnDynamicIpNetworkAddress());
    }

    @Test
    public void setVpnDynamicIpNetworkAddressTest() {
        String vpnDynamicIpNetworkAddress = "1.1.1.1";
        Assert.assertNull(this.vpnDynamicIpNetworkAddress);

        setVpnDynamicIpNetworkAddress(vpnDynamicIpNetworkAddress);
        Assert.assertEquals(vpnDynamicIpNetworkAddress, this.vpnDynamicIpNetworkAddress);
    }

    @Test
    public void getVpnDynamicIpSubnetMaskTest() {
        int vpnDynamicIpSubnetMask = 20;
        Assert.assertEquals(0, this.vpnDynamicIpSubnetMaskSuffix);

        this.vpnDynamicIpSubnetMaskSuffix = vpnDynamicIpSubnetMask;
        Assert.assertEquals(vpnDynamicIpSubnetMask, getVpnDynamicIpSubnetMaskSuffix());
    }

    @Test
    public void setVpnDynamicIpSubnetMaskTest() {
        int vpnDynamicIpSubnetMask = 20;
        Assert.assertEquals(0, this.vpnDynamicIpSubnetMaskSuffix);

        setVpnDynamicIpSubnetMaskSuffix(vpnDynamicIpSubnetMask);
        Assert.assertEquals(vpnDynamicIpSubnetMask, this.vpnDynamicIpSubnetMaskSuffix);
    }
}
