namespace java at.enfilo.def.communication.api.meta.thrift

/**
* MetaService - Provides basic information about a service (version and current timestamp)
*/
service MetaService {

    /**
    * Returns version of service
    */
    string getVersion();

    /**
    * Returns current timestamp (in millis) of service host.
    */
    i64 getTime();
}


