import {NodeInfo} from './node-info';
import {Feature} from "./feature";

export class ClusterInfo {
  id: string;
  name: string;
  managerId: string;
  cloudType: string;
  startTime: number;
  // TODO: list<string> activePrograms
  numberOfWorkers: number;
  numberOfReducers: number;
  defaultMapRoutineId: string;
  storeRoutineId: string;
  workers: NodeInfo[];
  reducers: NodeInfo[];
  features: Feature[];
  featureCount: Map<string, number>;
  averageLoad: number;
  averageNumberOfCores: number;
  numberOfTasks: number;
  numberOfQueuedTasks: number;
  numberOfRunningTasks: number;
  numberOfTasksToFinish: number;
  numberOfCores: number;
  host: string;
  maxLoadForCluster: number;
  averageLoadPercent: number;
  averageNumberOfCoresPercent: number;

  constructor(jsonData: any) {
    this.id = jsonData.id;
    this.name = jsonData.name;
    this.managerId = jsonData.managerId;
    this.cloudType = jsonData.cloudType;
    this.startTime = jsonData.startTime;
    this.numberOfWorkers = jsonData.numberOfWorkers;
    this.numberOfReducers = jsonData.numberOfReducers;
    this.defaultMapRoutineId = jsonData.defaultMapRoutineId;
    this.storeRoutineId = jsonData.storeRoutineId;
    this.host = jsonData.host;

    this.workers = [];
    this.reducers = [];
    this.features = [];
    this.featureCount = new Map<string, number>();
  }

  setWorkers(workers: NodeInfo[]): void {
    this.workers = workers;
    if (workers.length > 0) {
      this.updateCluster();
    }
  }

  setReducers(reducers: NodeInfo[]): void {
    this.reducers = reducers;
    if (reducers.length > 0) {
      this.updateCluster();
    }
  }

  private updateCluster(): void {
    let nrOfCores = 0;
    let load = 0;
    let nrOfTasks = 0;
    let nrOfQueuedTasks = 0;
    let nrOfRunningTasks = 0;
    const nodes = this.workers.concat(this.reducers);

    nodes.forEach(node => {
      nrOfCores += node.numberOfCores;
      load += node.load;
      nrOfTasks += node.numberOfTasksToFinish;
      nrOfQueuedTasks += node.numberOfQueuedTasks;
      nrOfRunningTasks += node.numberOfRunningTasks;
    });

    this.averageNumberOfCores = nrOfCores / nodes.length;
    this.averageLoad = load / nodes.length;
    this.numberOfTasks = nrOfTasks;
    this.numberOfQueuedTasks = nrOfQueuedTasks;
    this.numberOfRunningTasks = nrOfRunningTasks;
    this.numberOfTasksToFinish = nrOfQueuedTasks + nrOfRunningTasks;
    this.numberOfCores = nrOfCores;

    // calc max load
    this.maxLoadForCluster = this.averageNumberOfCores;
    if (this.averageLoad > this.maxLoadForCluster) {
      this.maxLoadForCluster = this.averageLoad;
    }
    this.maxLoadForCluster *= 1.1;

    // calc percent values
    this.averageLoadPercent = (this.averageLoad / this.maxLoadForCluster) * 100;
    this.averageNumberOfCoresPercent = (this.averageNumberOfCores / this.maxLoadForCluster) * 100;
  }

  adaptCalculatedValues(other: ClusterInfo): void {
    if (other != null) {
      this.numberOfRunningTasks = other.numberOfRunningTasks;
      this.numberOfQueuedTasks = other.numberOfQueuedTasks;
      this.numberOfTasksToFinish = other.numberOfTasksToFinish;
      this.numberOfTasks = other.numberOfTasks;
      this.numberOfCores = other.numberOfCores;
      this.numberOfWorkers = other.numberOfWorkers;
      this.numberOfReducers = other.numberOfReducers;
      this.averageLoad = other.averageLoad;
      this.averageNumberOfCores = other.averageNumberOfCores;
      this.averageLoadPercent = other.averageLoadPercent;
      this.averageNumberOfCoresPercent = other.averageNumberOfCoresPercent;
    }
  }

  setFeatures(features: Feature[]) {
    if (!features) {
      return;
    }
    this.featureCount.clear();
    let tmpFeatures = new Map<string, Feature>();

    for (let f of features) {
      this.increaseCount(f.id);
      if (!tmpFeatures.has(f.id)) {
        tmpFeatures.set(f.id, f);
      }

      // Extensions
      for (let e of f.extensions) {
        this.increaseCount(e.id);
        let addExtension = true;
        for (let e1 of tmpFeatures.get(f.id).extensions) {
          if (e.id === e1.id) {
            addExtension = false;
            break;
          }
        }
        if (addExtension) {
          tmpFeatures.get(f.id).extensions.push(e);
        }
      }
    }

    this.features = Array.from(tmpFeatures.values());
  }

  private increaseCount(id: string) {
    let count = 1;
    if (this.featureCount.has(id)) {
      count += this.featureCount.get(id);
      this.featureCount.set(id, count);
    } else {
      this.featureCount.set(id, count);
    }
  }
}

