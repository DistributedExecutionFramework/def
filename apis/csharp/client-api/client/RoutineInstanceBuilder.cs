using System;
using System.Collections.Generic;
using System.IO;
using Thrift.Protocol;
using Thrift.Transport;
namespace client_api.client
{
    public class RoutineInstanceBuilder
    {
        private RoutineInstanceDTO routineInstance;
        private Dictionary<String, ResourceDTO> inParams;

        public RoutineInstanceBuilder(String routineId)
        {
            routineInstance = new RoutineInstanceDTO();
            routineInstance.RoutineId = routineId;
            inParams = new Dictionary<string, ResourceDTO>();
        }

        public RoutineInstanceBuilder AddParameter(String name, String dataTypeId, TBase value)
        {
            ResourceDTO resource = new ResourceDTO();
            resource.DataTypeId = dataTypeId;
            resource.Data = Serialize(value);
            inParams.Add(name, resource);
            return this;
        }

        public RoutineInstanceBuilder AddParameter(String name, String sharedResourceId)
        {
            ResourceDTO sharedResource = new ResourceDTO();
            sharedResource.Id = sharedResourceId;
            inParams.Add(name, sharedResource);
            return this;
        }

        public RoutineInstanceDTO Build()
        {
            routineInstance.InParameters = inParams;
            return routineInstance;
        }

        // from: https://stackoverflow.com/questions/24988527/tserializer-serializer-new-tserializer-in-c-sharp
        private byte[] Serialize(TBase obj)
        {
            var stream = new MemoryStream();
            TProtocol tProtocol = new TBinaryProtocol(new TStreamTransport(stream, stream));
            obj.Write(tProtocol);
            return stream.ToArray();
        }
    }
}
