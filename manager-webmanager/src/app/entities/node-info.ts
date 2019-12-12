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
  numberOfRunningElements: number;
  numberOfQueuedElements: number;
  runningElements: string[];
  numberOfElementsToFinish: number;
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
    this.numberOfRunningElements = jsonData.parameters['numberOfRunningElements'] && +jsonData.parameters['numberOfRunningElements'] || 0;
    this.numberOfQueuedElements = jsonData.parameters['numberOfQueuedElements'] && +jsonData.parameters['numberOfQueuedElements'] || 0;
    if (jsonData.parameters['runningElements']) {
      this.runningElements = jsonData.parameters['runningElements'].split(' ');
    } else {
      this.runningElements = [];
    }
    this.storeRoutineId = jsonData.parameters['storeRoutineId'] || '';
    this.numberOfElementsToFinish = this.numberOfRunningElements + this.numberOfQueuedElements;
    this.features = [];
  }
}
