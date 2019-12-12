export class Program {
  id: string;
  state: string;
  createTime: number;
  finishTime: number;
  masterLibraryRoutine: boolean;
  userId: string;
  name: string;
  description: string;
  nrOfJobs: number;
  nrOfFinishedJobs: number;
  runtime: number;

  constructor(jsonData: any) {
    this.id = jsonData.id;
    this.state = jsonData.state;
    this.createTime = jsonData.createTime;
    this.finishTime = jsonData.finishTime;
    this.masterLibraryRoutine = jsonData.masterLibraryRoutine;
    this.nrOfJobs = jsonData.nrOfJobs;
    jsonData.userId ? this.userId = jsonData.userId : this.userId = '';
    jsonData.name ? this.name = jsonData.name : this.name = '';
    jsonData.description ? this.description = jsonData.description : this.description = '';
  }

  setRuntime(runtime: number) {
    this.runtime = runtime;
  }

  isFinished(): boolean {
    return (this.state === 'SUCCESS' || this.state === 'FAILED');
  }
}
