namespace java at.enfilo.def.cloud.communication.dto
namespace py def_api.thrift.cloud.communication.dto

enum InstanceTypeDTO {
    CLUSTER = 0,
    WORKER = 1,
    REDUCER = 2;
}

enum SupportedCloudEnvironment {
    AWS = 0;
}

struct AWSSpecificationDTO {
    1: string accessKeyID;
    2: string secretKey;
    3: string region;
    4: string publicSubnetId;
    5: string privateSubnetId;
    6: string vpcId;
    7: string keypairName;
    8: string vpnDynamicIpNetworkAddress;
    9: i32 vpnDynamicIpSubnetMaskSuffix;
    10: string clusterImageId;
    11: string clusterInstanceSize;
    12: string workerImageId;
    13: string workerInstanceSize;
    14: string reducerImageId;
    15: string reducerInstanceSize;
}