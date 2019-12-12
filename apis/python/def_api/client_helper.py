import base64

from thrift import TSerialization

from def_api.thrift.transfer.ttypes import RoutineInstanceDTO, ResourceDTO


class RoutineInstanceBuilder(object):
    def __init__(self, routine_id):
        self._routine_instance = RoutineInstanceDTO()
        self._routine_instance.routineId = routine_id
        self._routine_instance.inParameters = {}
        self._routine_instance.missingParameters = []

    def add_parameter(self, name, data):
        resource = ResourceDTO()
        # convention: every DataType must have a _id field
        resource.dataTypeId = getattr(data, '_id')
        resource.data = TSerialization.serialize(data)
        self._routine_instance.inParameters[name] = resource

    def add_shared_resource_parameter(self, name, shared_resource_id):
        resource = ResourceDTO()
        resource.id = shared_resource_id
        self._routine_instance.inParameters[name] = resource

    def add_missing_parameter(self, name):
        self._routine_instance.missingParameters.append(name)

    def get_routine_instance(self):
        return self._routine_instance


def extract_result(task, data_type_instance, index=0):
    resource = task.outParameters[index]
    if resource:
        if isinstance(resource.data, bytes):
            result = TSerialization.deserialize(data_type_instance, resource.data)
        elif isinstance(resource.data, str):
            result = TSerialization.deserialize(data_type_instance, base64.b64decode(resource.data))
        return result
    else:
        return None


def extract_input_param(task, param_name, data_type_instance):
    resource = task.inParameters[param_name]
    if resource:
        if isinstance(resource.data, bytes):
            param = TSerialization.deserialize(data_type_instance, resource.data)
        elif isinstance(resource.data, str):
            param = TSerialization.deserialize(data_type_instance, base64.b64decode(resource.data))
        return param
    else:
        return None
