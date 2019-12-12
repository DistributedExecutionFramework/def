using System;
using System.Collections.Generic;
using common.thrift;
using Thrift.Protocol;

namespace common.util
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

        public RoutineInstanceBuilder AddParameter<T>(String name, String dataTypeId, T value) where T : TBase
        {
            ResourceDTO resource = new ResourceDTO();
            resource.DataTypeId = dataTypeId;
            resource.Data = ThriftSerializer.Serialize(value);
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
    }
}
