import {NodeType} from "../enums/node-type.enum";
import {Feature} from "./feature";

export class NodeInfo {

  id: string;
  clusterId: string;
  numberOfCores: number;
  nodeType: NodeType;
  load: number;
  timestamp: number;
  host: string;
  numberOfQueues: number;
  numberOfRunningTasks: number;
  numberOfQueuedTasks: number;
  runningTasks: string[];
  numberOfTasksToFinish: number;
  storeRoutineId: string;
  features: Feature[];

  constructor(jsonData: any) {
    if (!jsonData) {
      return;
    }
    this.id = jsonData.id || '';
    this.clusterId = jsonData.cId || '';
    this.numberOfCores = jsonData.numberOfCores || '';
    this.nodeType = jsonData.type || '';
    this.load = jsonData.load || '';
    this.timestamp = jsonData.timeStamp || '';
    this.host = jsonData.host || '';
    this.numberOfQueues = jsonData.parameters['numberOfQueues'] && +jsonData.parameters['numberOfQueues'] || 0;
    this.numberOfRunningTasks = jsonData.parameters['numberOfRunningTasks'] && +jsonData.parameters['numberOfRunningTasks'] || 0;
    this.numberOfQueuedTasks = jsonData.parameters['numberOfQueuedTasks'] && +jsonData.parameters['numberOfQueuedTasks'] || 0;
    if (jsonData.parameters['runningTasks']) {
      this.runningTasks = jsonData.parameters['runningTasks'].split(' ');
    } else {
      this.runningTasks = [];
    }
    this.storeRoutineId = jsonData.parameters['storeRoutineId'] || '';
    this.numberOfTasksToFinish = this.numberOfRunningTasks + this.numberOfQueuedTasks;
    this.features = [];
  }
}
