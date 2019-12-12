import {Resource} from './resource';

export class Task {
  id: string;
  jobId: string;
  programId: string;
  state: string;
  startTime: number;
  createTime: number;
  finishTime: number;
  objectiveRoutineId: string;
  mapRoutineId: string;
  inParameters: Map<string, Resource>;
  outParameters: Resource[];
  runtime: number;
  messages: string[];

  constructor(jsonData: any) {
    this.id = jsonData.id;
    this.jobId = jsonData.jId;
    this.programId = jsonData.pId;
    this.state = jsonData.state;
    this.createTime = jsonData.createTime;
    this.startTime = jsonData.startTime;
    this.finishTime = jsonData.finishTime;
    this.runtime = jsonData.runtime;
    jsonData.mapRoutineId ? this.mapRoutineId = jsonData.mapRoutineId : this.mapRoutineId = '';
    jsonData.objectiveRoutineId ? this.objectiveRoutineId = jsonData.objectiveRoutineId : this.objectiveRoutineId = '';
    this.inParameters = new Map<string, Resource>();
    if (jsonData.inParameters !== undefined && jsonData.inParameters !== null) {
      const inParams = jsonData.inParameters;
      const keys = Object.keys(inParams);
      for (const key of keys) {
        this.inParameters.set(key, new Resource(inParams[key]));
      }
    }
    this.outParameters = [];
    if (jsonData.outParameters !== undefined && jsonData.outParameters !== null) {
      const outParams = jsonData.outParameters;
      outParams.forEach(obj => this.outParameters.push(new Resource(obj)));
    }
    this.messages = jsonData.messages;
  }

  setRuntime(runtime: number): void {
    this.runtime = runtime;
  }

  isFinished(): boolean {
    return (this.state === 'SUCCESS' || this.state === 'FAILED');
  }

  hasAlreadyStarted(): boolean {
    if (this.state === 'RUN' || this.state === 'SUCCESS' || this.state === 'FAILED') {
      return true;
    }
    return false;
  }
}
