package at.enfilo.def.scheduler.worker.api.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerResponseService;

//@Path("/response")
public interface IWorkerSchedulerResponseService extends WorkerSchedulerResponseService.Iface, IResource {
}
