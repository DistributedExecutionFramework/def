import {Resource} from './resource';

export class Job {
  id: string;
  programId: string;
  state: string;
  createTime: number;
  startTime: number;
  finishTime: number;
  nrOfScheduledTasks: number;
  nrOfRunningTasks: number;
  nrOfSuccessfulTasks: number;
  nrOfFailedTasks: number;
  mapRoutineId: string;
  reduceRoutineId: string;
  reducedResults: Resource[];
  runtime: number;

  constructor(jsonData: any) {
    this.id = jsonData.id;
    this.programId = jsonData.pId;
    this.state = jsonData.state;
    this.createTime = jsonData.createTime;
    this.startTime = jsonData.startTime;
    this.finishTime = jsonData.finishTime;
    this.nrOfSuccessfulTasks = jsonData.successfulTasks;
    this.nrOfFailedTasks = jsonData.failedTasks;
    this.nrOfRunningTasks = jsonData.runningTasks;
    this.nrOfScheduledTasks = jsonData.scheduledTasks;
    this.mapRoutineId = jsonData.mapRoutineId;
    jsonData.reduceRoutineId ? this.reduceRoutineId = jsonData.reduceRoutineId : this.reduceRoutineId = '';
    this.reducedResults = [];
    if (jsonData.reducedResults !== undefined && jsonData.reducedResults !== null) {
      const results = jsonData.reducedResults;
      results.forEach(obj => this.reducedResults.push(new Resource(obj)));
    }
  }

  getNrOfTasks(): number {
    return (this.nrOfScheduledTasks + this.nrOfRunningTasks + this.nrOfSuccessfulTasks + this.nrOfFailedTasks);
  }

  getNrOfFinishedTasks(): number {
    return (this.nrOfFailedTasks + this.nrOfSuccessfulTasks);
  }

  setRuntime(runtime: number) {
    this.runtime = runtime;
  }

  isFinished(): boolean {
    return (this.state === 'SUCCESS' || this.state === 'FAILED');
  }

  hasJobAlreadyStarted() {
    if (this.state === 'RUN' || this.state === 'SUCCESS' || this.state === 'FAILED') {
      return true;
    }
    return false;
  }
}
