package at.enfilo.def.client.shell;

class Constants {
	final static String PROMPT_CONNECTED = "\n%s @ %s:%d%s\n>> ";
	final static String PROMPT_NOT_CONNECTED = "\nnot-connected\n>> ";
	final static String PROMPT_PROVIDER_NAME = "DEFShell Prompt Provider";

	final static String BANNER =
			"\n" +
			" ██████╗ ███████╗███████╗\n" +
			" ██╔══██╗██╔════╝██╔════╝\n" +
			" ██║  ██║█████╗  █████╗  \n" +
			" ██║  ██║██╔══╝  ██╔══╝  \n" +
			" ██████╔╝███████╗██║     \n" +
			" ╚═════╝ ╚══════╝╚═╝     \n";
	final static String WELCOME_MSG = "Welcome to DEFShell.";
	final static String VERSION = "1.5.0";
	final static String BANNER_PROVIDER_NAME = "DEFShell";

	final static String HISTORY_FILE_NAME = "defshell.log";
	final static String HISTORY_PROVIDER_NAME = "DEFShell History Provider";

	// Options

	final static String OPT_SERVICE = "service";
	final static String OPT_HOST = "host";
	final static String OPT_PORT = "port";
	final static String OPT_PROTOCOL = "protocol";
	final static String OPT_URL_PATTERN = "url-pattern";
	final static String OPT_CLUSTER_ID = "clusterId";
	final static String OPT_TO_OBJECT = "toObject";
	final static String OPT_NUMBER_OF_WORKERS = "numberWorkers";
	final static String OPT_NUMBER_OF_REDUCERS = "numberReducers";
	final static String OPT_MANAGER_ID = "managerId";
	final static String OPT_NODE_ID = "nodeId";
	final static String OPT_NAME = "name";
	final static String OPT_TYPE = "type";
	final static String OPT_VERSION = "version";
	final static String OPT_GROUP = "group";
	final static String OPT_PERIODICALLY = "periodically";
	final static String OPT_PERIOD_DURATION = "periodDuration";
	final static String OPT_PERIOD_UNIT = "periodUnit";
	final static String OPT_QUEUE_ID = "queueId";
	final static String OPT_TASKS = "tasks";
	final static String OPT_TASK_IDS = "tIds";
	final static String OPT_TASK_ID = "tId";
	final static String OPT_SERVICE_ENDPOINT = "serviceEndpoint";
	final static String OPT_USER_ID = "userId";
	final static String OPT_PROGRAM_ID = "pId";
	final static String OPT_JOB_ID = "jId";
	final static String OPT_FEATURE_ID = "fId";
    final static String OPT_ROUTINE_INSTANCE = "routineInstance";
    final static String OPT_ROUTINE_ID = "routineId";
    final static String OPT_ROUTINE_BINARY_ID = "routineBinaryId";
	final static String OPT_IN_PARAMS = "inParameters";
	final static String OPT_DATA_TYPE = "dataType";
	final static String OPT_RESOURCE_ID = "resourceId";
	final static String OPT_MISSING_PARAMS = "missingParams";
	final static String OPT_VALUE = "value";
	final static String OPT_DATA = "data";
	final static String OPT_TASK = "task";
	final static String OPT_JOB = "job";
	final static String OPT_SEARCH_PATTERN = "pattern";
	final static String OPT_ROUTINE = "routine";
	final static String OPT_FILE_NAME = "fileName";
	final static String OPT_PRIMARY = "primary";
	final static String OPT_DATATYPE_ID = "dataTypeId";
	final static String OPT_LABEL = "label";
	final static String OPT_DESCRIPTION = "description";
	final static String OPT_NODE_TYPE = "nodeType";
	final static String OPT_EXECUTION_STATE = "state";
	final static String OPT_SORTING_CRITERION = "sortingCriterion";
	final static String OPT_CLOUD_SPECIFICATION = "cloudSpecification";
	final static String OPT_CLOUD_ENVIRONMENT = "cloudEnvironment";
	final static String OPT_AWS_ACCESS_KEY_ID = "accessKeyId";
	final static String OPT_AWS_SECRET_KEY = "secretKey";
	final static String OPT_AWS_REGION = "region";
	final static String OPT_AWS_PUBLIC_SUBNET_ID = "publicSubnetId";
	final static String OPT_AWS_PRIVATE_SUBNET_ID = "privateSubnetId";
	final static String OPT_AWS_VPC_ID = "vpcIc";
	final static String OPT_AWS_KEYPAIR_NAME = "keypairName";
	final static String OPT_VPN_DYNAMIC_IP_NETWORK_ADDRESS = "dynamicIPNetworkAddress";
	final static String OPT_VPN_DYNAMIC_IP_SUBNET_MASK = "dynamicIPSubnetMask";
	final static String OPT_CLUSTER_IMAGE_ID = "clusterImageId";
	final static String OPT_CLUSTER_INSTANCE_SIZE = "clusterInstanceSize";
	final static String OPT_WORKER_IMAGE_ID = "workerImageId";
	final static String OPT_WORKER_INSTANCE_SIZE = "workerInstanceSize";
	final static String OPT_REDUCER_IMAGE_ID = "reducerImageId";
	final static String OPT_REDUCER_INSTANCE_SIZE = "reducerInstanceSize";
	final static String OPT_NEW_NODE_POOL_SIZE = "newNodePoolSize";

	// Objects

	final static String CMD_OBJECT_PREFIX = "object";

	final static String CMD_OBJECT_LIST = CMD_OBJECT_PREFIX + " " + "list";
	final static String CMD_OBJECT_CREATE = CMD_OBJECT_PREFIX + " " + "create";
	final static String CMD_OBJECT_SHOW = CMD_OBJECT_PREFIX + " " + "show";
	final static String CMD_OBJECT_REMOVE = CMD_OBJECT_PREFIX + " " + "remove";
	final static String CMD_OBJECT_UPDATE_ENDPOINT = CMD_OBJECT_PREFIX + " " + "update service-endpoint";
	final static String CMD_OBJECT_UPDATE_ROUTINE_INSTANCE = CMD_OBJECT_PREFIX + " " + "update routine-instance";
	final static String CMD_OBJECT_UPDATE_DEF_STRING = CMD_OBJECT_PREFIX + " " + "update def-string";
	final static String CMD_OBJECT_UPDATE_DEF_INTEGER = CMD_OBJECT_PREFIX + " " + "update def-integer";
	final static String CMD_OBJECT_UPDATE_DEF_DOUBLE = CMD_OBJECT_PREFIX + " " + "update def-double";
	final static String CMD_OBJECT_UPDATE_DEF_BOOLEAN = CMD_OBJECT_PREFIX + " " + "update def-boolean";
	final static String CMD_OBJECT_UPDATE_RESOURCE = CMD_OBJECT_PREFIX + " " + "update resource";
	final static String CMD_OBJECT_UPDATE_AWS_SPECIFICATION = CMD_OBJECT_PREFIX + " " + "update AWS specification";

	final static String MESSAGE_OBJECT_CREATED = "%s with name %s created.";
	final static String MESSAGE_OBJECT_ALREADY_EXISTS = "Object with name %s already exists.";
	final static String MESSAGE_OBJECT_NOT_EXISTS = "Object with name %s does not exists.";
	final static String MESSAGE_OBJECT_REMOVE = "%s removed.";
	final static String MESSAGE_OBJECT_UPDATED = "%s updated.";
	final static String MESSAGE_OBJECT_WRONG_TYPE = "Object %s is not %s.";
	final static String MESSAGE_OBJECT_STORED = "Stored output to object %s.";


	// Service

	final static String CMD_SERVICE_PREFIX = "service";

	final static String CMD_SERVICE_SWITCH = CMD_SERVICE_PREFIX + " " + "switch";
	final static String CMD_SERVICE_VERSION = CMD_SERVICE_PREFIX + " " + "version";
	final static String CMD_SERVICE_TIME = CMD_SERVICE_PREFIX + " " + "time";
	final static String CMD_SERVICE_PING = CMD_SERVICE_PREFIX + " " + "ping";

	final static String MESSAGE_SERVICE_SWITCHED = "Switched to service %s @ %s:%d%s.";
	final static String MESSAGE_SERVICE_ENDPOINT_OR_DIRECT = "Use either option endpoint or (host, port and protocol).";
	final static String MESSAGE_SERVICE_VERSION = "Version of service %s: %s";
	final static String MESSAGE_SERVICE_TIME = "Time on service %s: %s";
	final static String MESSAGE_SERVICE_PING = "Ping results to service %s: %d ms, %d ms, %d ms";
	final static String MESSAGE_SERVICE_NOT_AVAIL = "Requested service is not available: %s";

	// Library Service (incl. LibraryAdmin)

	final static String CMD_LIBRARY_PREFIX = "library";

	final static String CMD_LIBRARY_ROUTINE_SHOW = CMD_LIBRARY_PREFIX + " " + "routine show";
	final static String CMD_LIBRARY_ROUTINE_FIND = CMD_LIBRARY_PREFIX + " " + "routine find";
	final static String CMD_LIBRARY_ROUTINE_REMOVE = CMD_LIBRARY_PREFIX + " " + "routine remove";
	final static String CMD_LIBRARY_ROUTINE_CREATE = CMD_LIBRARY_PREFIX + " " + "routine create";
	final static String CMD_LIBRARY_ROUTINE_UPDATE = CMD_LIBRARY_PREFIX + " " + "routine update";
	final static String CMD_LIBRARY_ROUTINE_BINARY_UPLOAD = CMD_LIBRARY_PREFIX + " " + "routine binary upload";
	final static String CMD_LIBRARY_ROUTINE_BINARY_REMOVE = CMD_LIBRARY_PREFIX + " " + "routine binary remove";
	final static String CMD_LIBRARY_DATA_TYPE_FIND = CMD_LIBRARY_PREFIX + " " + "data-type find";
	final static String CMD_LIBRARY_DATA_TYPE_CREATE = CMD_LIBRARY_PREFIX + " " + "data-type create";
	final static String CMD_LIBRARY_DATA_TYPE_SHOW = CMD_LIBRARY_PREFIX + " " + "data-type show";
	final static String CMD_LIBRARY_DATA_TYPE_REMOVE = CMD_LIBRARY_PREFIX + " " + "data-type remove";
	final static String CMD_LIBRARY_TAG_FIND = CMD_LIBRARY_PREFIX + " " + "tag find";
	final static String CMD_LIBRARY_TAG_CREATE = CMD_LIBRARY_PREFIX + " " + "tag create";
	final static String CMD_LIBRARY_TAG_REMOVE = CMD_LIBRARY_PREFIX + " " + "tag remove";
	final static String CMD_LIBRARY_MASTER_LIBRARY_SET = CMD_LIBRARY_PREFIX + " " + "master-library set";
	final static String CMD_LIBRARY_MASTER_LIBRARY_SHOW = CMD_LIBRARY_PREFIX + " " + "master-library show";
	final static String CMD_LIBRARY_FEATURE_CREATE = CMD_LIBRARY_PREFIX + " " + "feature create";
	final static String CMD_LIBRARY_FEATURE_FIND = CMD_LIBRARY_PREFIX + " " + "feature find";
	final static String CMD_LIBRARY_FEATURE_SHOW = CMD_LIBRARY_PREFIX + " " + "feature show";
	final static String CMD_LIBRARY_EXTENSION_CREATE = CMD_LIBRARY_PREFIX + " " + "extension create";

	final static String MESSAGE_LIBRARY_ROUTINE_CREATED = "Routine successful created with Routine-Id %s.";
	final static String MESSAGE_LIBRARY_ROUTINE_REMOVE = "Routine remove done";
	final static String MESSAGE_LIBRARY_ROUTINE_UPDATED = "Routine updated, new Routine-Id %s.";
	final static String MESSAGE_LIBRARY_ROUTINE_BINARY_UPLOAD = "RoutineBinary upload done, Binary-Id %s.";
	final static String MESSAGE_LIBRARY_ROUTINE_BINARY_REMOVE = "RoutineBinary remove done";
	final static String MESSAGE_LIBRARY_DATA_TYPE_CREATED = "DataType successful created with Id %s.";
	final static String MESSAGE_LIBRARY_DATA_TYPE_REMOVE = "DataType remove done";
	final static String MESSAGE_LIBRARY_TAG_CREATED = "Tag %s create done";
	final static String MESSAGE_LIBRARY_TAG_REMOVE = "Tag %s remove done";
	final static String MESSAGE_LIBRARY_DATA_ENDPOINT_SET = "MasterLibrary endpoint of library change done.";
	final static String MESSAGE_LIBRARY_FEATURE_CREATED = "Feature with id %s created.";
	final static String MESSAGE_LIBRARY_EXTENSION_CREATED = "Extension with id %s created.";

	// Manager Service

	final static String CMD_MANAGER_PREFIX = "manager";

	final static String CMD_MANAGER_CLUSTER_LIST = CMD_MANAGER_PREFIX + " " + "cluster list";
	final static String CMD_MANAGER_CLUSTER_SHOW = CMD_MANAGER_PREFIX + " " + "cluster show";
	final static String CMD_MANAGER_CLUSTER_ENDPOINT = CMD_MANAGER_PREFIX + " " + "cluster endpoint";
	final static String CMD_MANAGER_CLUSTER_CREATE = CMD_MANAGER_PREFIX + " " + "cluster create";
	final static String CMD_MANAGER_CLUSTER_ADD_DIRECT = CMD_MANAGER_PREFIX + " " + "cluster add-direct";
	final static String CMD_MANAGER_CLUSTER_ADD_ENDPOINT = CMD_MANAGER_PREFIX + " " + "cluster add-endpoint";
	final static String CMD_MANAGER_CLUSTER_REMOVE = CMD_MANAGER_PREFIX + " " + "cluster remove";
	final static String CMD_MANAGER_CLUSTER_ADJUST_NODE_POOL_SIZE = CMD_MANAGER_PREFIX + " " + "cluster adjust-node-pool-size";

	final static String MESSAGE_MANAGER_CLUSTER_CREATED = "New Cluster with Id %s created.";
	final static String MESSAGE_MANAGER_ADD_CLUSTER = "Add Cluster done";
	final static String MESSAGE_MANAGER_REMOVE_CLUSTER = "Delete Cluster done";
	final static String MESSAGE_MANAGER_NODE_POOL_SIZE_ADJUSTED = "Node pool size of cluster adjust done";


	// Cluster Service

	final static String CMD_CLUSTER_PREFIX = "cluster";

	final static String CMD_CLUSTER_TAKE_CONTROL = CMD_CLUSTER_PREFIX + " " + "take-control";
	final static String CMD_CLUSTER_SHOW = CMD_CLUSTER_PREFIX + " " + "info";
	final static String CMD_CLUSTER_DESTROY = CMD_CLUSTER_PREFIX + " " + "destroy";
	final static String CMD_CLUSTER_NODE_LIST = CMD_CLUSTER_PREFIX + " " + "node list";
	final static String CMD_CLUSTER_NODE_SHOW = CMD_CLUSTER_PREFIX + " " + "node show";
	final static String CMD_CLUSTER_NODE_ADD_DIRECT = CMD_CLUSTER_PREFIX + " " + "node add-direct";
	final static String CMD_CLUSTER_NODE_ADD_ENDPOINT = CMD_CLUSTER_PREFIX + " " + "node add-endpoint";
	final static String CMD_CLUSTER_NODE_REMOVE = CMD_CLUSTER_PREFIX + " " + "node remove";
	final static String CMD_CLUSTER_NODE_GET_ENDPOINT = CMD_CLUSTER_PREFIX + " " + "node get-endpoint";
	final static String CMD_CLUSTER_SCHEDULER_GET_ENDPOINT = CMD_CLUSTER_PREFIX + " " + "scheduler get-endpoint";
	final static String CMD_CLUSTER_SCHEDULER_SET_ENDPOINT = CMD_CLUSTER_PREFIX + " " + "scheduler set-endpoint";
	final static String CMD_CLUSTER_ROUTINE_SET_MAP = CMD_CLUSTER_PREFIX + " " + "routine set-default-map";
	final static String CMD_CLUSTER_ROUTINE_SET_STORE = CMD_CLUSTER_PREFIX + " " + "routine set-store";


	final static String MESSAGE_CLUSTER_TAKE_CONTROL = "Take control from Manager done";
	final static String MESSAGE_CLUSTER_DESTROYED = "Cluster destroyed.";
	final static String MESSAGE_CLUSTER_ADD_NODE = "Add Node done";
	final static String MESSAGE_CLUSTER_REMOVE_NODE = "Remove Node done";
	final static String MESSAGE_CLUSTER_SET_SCHEDULER = "Set Scheduler ServiceEndpoint done";
	final static String MESSAGE_CLUSTER_ROUTINE_SET_MAP = "Set MapRoutine done";
	final static String MESSAGE_CLUSTER_ROUTINE_SET_STORE = "Set StoreRoutine done";


	// Worker Service

	final static String CMD_WORKER_PREFIX = "worker";

	final static String CMD_WORKER_TAKE_CONTROL = CMD_WORKER_PREFIX + " " + "take-control";
	final static String CMD_WORKER_REGISTER_OBSERVER = CMD_WORKER_PREFIX + " " + "register-observer";
	final static String CMD_WORKER_DEREGISTER_OBSERVER = CMD_WORKER_PREFIX + " " + "deregister-observer";
	final static String CMD_WORKER_SHUTDOWN = CMD_WORKER_PREFIX + " " + "shutdown";
	final static String CMD_WORKER_SHOW = CMD_WORKER_PREFIX + " " + "info";
	final static String CMD_WORKER_ENV = CMD_WORKER_PREFIX + " " + "env";
	final static String CMD_WORKER_QUEUE_LIST = CMD_WORKER_PREFIX + " " + "queue list";
	final static String CMD_WORKER_QUEUE_CREATE = CMD_WORKER_PREFIX + " " + "queue create";
	final static String CMD_WORKER_QUEUE_SHOW = CMD_WORKER_PREFIX + " " + "queue show";
	final static String CMD_WORKER_QUEUE_REMOVE = CMD_WORKER_PREFIX + " " + "queue remove";
	final static String CMD_WORKER_QUEUE_RELEASE = CMD_WORKER_PREFIX + " " + "queue release";
	final static String CMD_WORKER_QUEUE_PAUSE = CMD_WORKER_PREFIX + " " + "queue pause";
	final static String CMD_WORKER_TASKS_QUEUE = CMD_WORKER_PREFIX + " " + "tasks queue";
	final static String CMD_WORKER_TASKS_LIST = CMD_WORKER_PREFIX + " " + "tasks list";
	final static String CMD_WORKER_TASKS_MOVE = CMD_WORKER_PREFIX + " " + "tasks move";
	final static String CMD_WORKER_TASKS_MOVE_ALL = CMD_WORKER_PREFIX + " " + "tasks move-all";
	final static String CMD_WORKER_TASKS_FETCH_FINISHED = CMD_WORKER_PREFIX + " " + "tasks fetch-finished";
	final static String CMD_WORKER_STORE_ROUTINE_SHOW = CMD_WORKER_PREFIX + " " + "store-routine show";
	final static String CMD_WORKER_STORE_ROUTINE_SET = CMD_WORKER_PREFIX + " " + "store-routine set";

	final static String MESSAGE_WORKER_TAKE_CONTROL = "Take control from Cluster done";
	final static String MESSAGE_WORKER_REGISTER_OBSERVER = "Register observer done";
	final static String MESSAGE_WORKER_DEREGISTER_OBSERVER = "Deregister observer done";
	final static String MESSAGE_WORKER_SHUTDOWN = "Worker shutdown sent.";
	final static String MESSAGE_WORKER_QUEUE_CREATED = "Queue creation done";
	final static String MESSAGE_WORKER_QUEUE_REMOVE = "Queue remove done";
	final static String MESSAGE_WORKER_QUEUE_RELEASED = "Queue release done";
	final static String MESSAGE_WORKER_QUEUE_PAUSED = "Queue pause done";
	final static String MESSAGE_WORKER_TASKS_QUEUE = "Queue release done";
	final static String MESSAGE_WORKER_TASKS_MOVE = "Move Tasks done";
	final static String MESSAGE_STORE_ROUTINE_SET = "Partition-routine updated done";

	// Exec-Logic Service

	final static String CMD_EXEC_PREFIX = "exec-logic";

	final static String CMD_EXEC_PROGRAM_LIST = CMD_EXEC_PREFIX + " " + "program list";
	final static String CMD_EXEC_PROGRAM_CREATE = CMD_EXEC_PREFIX + " " + "program create";
	final static String CMD_EXEC_PROGRAM_SHOW = CMD_EXEC_PREFIX + " " + "program show";
	final static String CMD_EXEC_PROGRAM_REMOVE = CMD_EXEC_PREFIX + " " + "program remove";
	final static String CMD_EXEC_PROGRAM_MARK_FINISHED = CMD_EXEC_PREFIX + " " + "program mark-finished";
	final static String CMD_EXEC_JOB_LIST = CMD_EXEC_PREFIX + " " + "job list";
	final static String CMD_EXEC_JOB_CREATE = CMD_EXEC_PREFIX + " " + "job create";
	final static String CMD_EXEC_JOB_SHOW = CMD_EXEC_PREFIX + " " + "job show";
	final static String CMD_EXEC_JOB_REMOVE = CMD_EXEC_PREFIX + " " + "job remove";
	final static String CMD_EXEC_JOB_SHOW_MAP_ROUTINE = CMD_EXEC_PREFIX + " " + "job show-map-routine";
	final static String CMD_EXEC_JOB_ATTACH_MAP_ROUTINE = CMD_EXEC_PREFIX + " " + "job attach-map-routine";
	final static String CMD_EXEC_JOB_SHOW_REDUCE_ROUTINE = CMD_EXEC_PREFIX + " " + "job show-reduce-routine";
	final static String CMD_EXEC_JOB_ATTACH_REDUCE_ROUTINE = CMD_EXEC_PREFIX + " " + "job attach-reduce-routine";
	final static String CMD_EXEC_JOB_MARK_COMPLETE = CMD_EXEC_PREFIX + " " + "job mark-complete";
	final static String CMD_EXEC_JOB_ABORT = CMD_EXEC_PREFIX + " " + "job abort";
	final static String CMD_EXEC_TASK_LIST = CMD_EXEC_PREFIX + " " + "task list";
	final static String CMD_EXEC_TASK_CREATE = CMD_EXEC_PREFIX + " " + "task create";
	final static String CMD_EXEC_TASK_SHOW = CMD_EXEC_PREFIX + " " + "task show";
	final static String CMD_EXEC_TASK_RERUN = CMD_EXEC_PREFIX + " " + "task rerun";
	final static String CMD_EXEC_TASK_ABORT = CMD_EXEC_PREFIX + " " + "task abort";
	final static String CMD_EXEC_SHARED_RESOURCE_LIST = CMD_EXEC_PREFIX + " " + "shared-resource list";
	final static String CMD_EXEC_SHARED_RESOURCE_CREATE = CMD_EXEC_PREFIX + " " + "shared-resource create";
	final static String CMD_EXEC_SHARED_RESOURCE_SHOW = CMD_EXEC_PREFIX + " " + "shared-resource show";
	final static String CMD_EXEC_SHARED_RESOURCE_REMOVE = CMD_EXEC_PREFIX + " " + "shared-resource remove";

	final static String MESSAGE_EXEC_PROGRAM_CREATED = "Program with id %s on Cluster %s created.";
	final static String MESSAGE_EXEC_PROGRAM_REMOVE = "Program remove done";
	final static String MESSAGE_EXEC_PROGRAM_MARK_FINISHED = "Program mark as finished done";
	final static String MESSAGE_EXEC_JOB_CREATED = "Job with id %s created.";
	final static String MESSAGE_EXEC_JOB_REMOVE = "Job remove done";
	final static String MESSAGE_EXEC_JOB_ABORTED = "Job abortion done";
	final static String MESSAGE_EXEC_JOB_GET_MAP_ROUTINE = "Attached MapRoutine Id of Job %s: %s";
	final static String MESSAGE_EXEC_JOB_ATTACH_MAP_ROUTINE = "Attach MapRoutine done";
	final static String MESSAGE_EXEC_JOB_GET_REDUCE_ROUTINE = "Attached ReduceRoutine Id of Job %s: %s";
	final static String MESSAGE_EXEC_JOB_ATTACH_REDUCE_ROUTINE = "Attach ReduceRoutine done";
	final static String MESSAGE_EXEC_JOB_MARK_COMPLETE = "Job mark as complete done";
	final static String MESSAGE_EXEC_TASK_CREATED = "Task with id %s created.";
	final static String MESSAGE_EXEC_TASK_ABORTED = "Task abort done";
	final static String MESSAGE_EXEC_TASK_RERUN = "Task re-run done";
	final static String MESSAGE_EXEC_RESOURCE_CREATED = "Resource with id %s created.";
	final static String MESSAGE_EXEC_RESOURCE_REMOVE = "Program remove done";


	// Scheduler Service

	final static String CMD_SCHEDULER_PREFIX = "scheduler";

	final static String CMD_SCHEDULER_SCHEDULE = CMD_SCHEDULER_PREFIX + " " + "schedule";
	final static String CMD_SCHEDULER_JOB_MARK_COMPLETE = CMD_SCHEDULER_PREFIX + " " + "job mark-complete";
	final static String CMD_SCHEDULER_JOB_REMOVE = CMD_SCHEDULER_PREFIX + " " + "job remove";
	final static String CMD_SCHEDULER_JOB_ADD = CMD_SCHEDULER_PREFIX + " " + "job add";
	final static String CMD_SCHEDULER_WORKER_ADD = CMD_SCHEDULER_PREFIX + " " + "worker add";
	final static String CMD_SCHEDULER_WORKER_REMOVE = CMD_SCHEDULER_PREFIX + " " + "worker remove";
}
