import {SortingCriterion} from "../enums/sorting-criterion.enum";

export class ServiceConfig {

  constructor(
    private protocol: string,
    private host: string,
    private port: string,
    private urlPattern: string
  ) {

  }

  public getConnectionUrl(): string {
    let host = '';
    if (this.host === 'localhost') {
      host = window.location.hostname;
    } else {
      host = this.host;
    }
    return this.protocol + '://' + host + ':' + this.port + this.urlPattern;
  }

  public getUrlForFetchingAllPrograms(userId: string): string {
    return this.getConnectionUrl() +
      '/programs/user/' + userId;
  }

  public getUrlForFetchingProgramWithId(id: string): string {
    return this.getConnectionUrl() +
      '/programs/' + id;
  }

  public getUrlForDeletingProgramWithId(id: string): string {
    return this.getConnectionUrl() +
      '/programs/' + id;
  }

  public getUrlForAbortingProgramWithId(id: string): string {
    return this.getConnectionUrl() +
      '/programs/' + id +
      '/abort';
  }

  public getUrlForUpdatingProgramWithId(id: string): string {
    return this.getConnectionUrl() +
      '/programs/' + id;
  }

  public getUrlForFetchingAllJobs(pId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs';
  }

  public getUrlForFetchingNrOfFinishedJobsOfProgramWithId(pId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/nrOfFinishedJobs';
  }

  public getUrlForFetchingJobWithId(pId: string, jId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId;
  }

  public getUrlForAbortingJobWithId(pId: string, jId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/abort';
  }

  public getUrlForFetchingTasksWithState(pId: string, jId: string, state: string, sortingCriterion: SortingCriterion, nrOfTasks: number): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/tasks/state/' + state +
      '/sort/' + SortingCriterion[sortingCriterion] +
      '/nrOfTasks/' + nrOfTasks;
  }

  public getUrlForFetchingTasksWithFilter(pId: string, jId: string, sortingCriterion: SortingCriterion) {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/tasks/sort/' + SortingCriterion[sortingCriterion] +
      '/filter';
  }

  public getUrlForFetchingTaskWithId(pId: string, jId: string, tId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/tasks/' + tId;
  }

  public getUrlForAbortingTaskWithId(pId: string, jId: string, tId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/tasks/' + tId +
      '/abort';
  }

  public getUrlForReRunTaskWithId(pId: string, jId: string, tId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/tasks/' + tId +
      '/reschedule';
  }

  public getUrlForFetchingAllClusters(): string {
    return this.getConnectionUrl() +
      '/clusters';
  }

  public getUrlForFetchingClusterWithId(cId: string): string {
    return this.getConnectionUrl() +
      '/clusters/' + cId;
  }

  public getUrlForFetchingAllWorkers(cId: string): string {
    return this.getConnectionUrl() +
      '/clusters/' + cId +
      '/workers';
  }

  public getUrlForFetchingAllReducers(cId: string): string {
    return this.getConnectionUrl() +
      '/clusters/' + cId +
      '/reducers';
  }

  public getUrlForFetchingClusterEnvironment(cId: string): string {
    return this.getConnectionUrl() +
      '/clusters/' + cId + '/environment';
  }

  public getUrlForFetchingNodeEnvironment(cId: string, nId: string): string {
    return this.getConnectionUrl() +
      '/clusters/' + cId + '/nodes/' + nId + '/environment';
  }

  public getUrlForFetchingNodeWithId(cId: string, nId: string): string {
    return this.getConnectionUrl() +
      '/clusters/' + cId +
      '/nodes/' + nId;
  }

  public getUrlForFetchingNameOfDataTypeWithId(dtId: string): string {
    return this.getConnectionUrl() +
      '/datatype/' + dtId;
  }

  public getUrlForFetchingRoutinesWithPattern(pattern: string): string {
    return this.getConnectionUrl() +
      '/library/routines?pattern=' + pattern;
  }

  public getUrlForFetchingRoutineWithId(rId: string): string {
    return this.getConnectionUrl() +
      '/library/routines/' + rId;
  }

  public getUrlForFetchingFeaturesWithPattern(pattern: string): string {
    return this.getConnectionUrl() +
      '/library/features?pattern=' + pattern;
  }

  public getUrlForFetchingDataTypesWithPattern(pattern: string): string {
    return this.getConnectionUrl() +
      '/library/datatypes?pattern=' + pattern;
  }

  public getUrlForFetchingDataTypeWithId(dId: string): string {
    return this.getConnectionUrl() +
      '/library/datatypes/' + dId;
  }

  public getUrlForCreatingRoutine(): string {
    return this.getConnectionUrl() +
      '/library/routines';
  }

  public getUrlForRemoveRoutine(rId: string): string {
    return this.getConnectionUrl() + '/library/routines/' + rId;
  }

  public getUrlForCreatingDataType(name: string): string {
    return this.getConnectionUrl() +
      '/library/datatypes?name=' + name;
  }

  public getUrlForGeneratingDataTypes(language: string): string {
    return this.getConnectionUrl() +
      '/library/datatypes/generate?language=' + language;
  }

  public getUrlForUploadRoutineBinary(rId: string, md5: string, sizeInBytes: number, primary: boolean): string {
    return this.getConnectionUrl() +
      '/library/routines/' + rId +
      '/binaries?md5=' + md5 +
      '&sizeInBytes=' + sizeInBytes +
      '&primary=' + primary;
  }

  public getUrlForFetchingDataValueOfTaskInputParameter(pId: string, jId: string, tId: string, inParamName: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/tasks/' + tId +
      '/inParam/' + inParamName;
  }

  public getUrlForFetchingDataValueOfTaskOutputParameter(pId: string, jId: string, tId: string, outParamId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/tasks/' + tId +
      '/outParam/' + outParamId;
  }

  public getUrlForFetchingDataValueOfJobReducedResult(pId: string, jId: string, reducedResultId: string): string {
    return this.getConnectionUrl() +
      '/programs/' + pId +
      '/jobs/' + jId +
      '/reduce/' + reducedResultId;
  }

  public getUrlForAddExtension(baseFeatureId: string, name: string, version: string): string {
    return this.getConnectionUrl() + '/library/features/' + baseFeatureId + '/extensions?name=' + name + '&version=' + version;
  }

  public getUrlForCreateFeature(name: string, version: string, group: string): string {
    let url = this.getConnectionUrl() + '/library/features?name=' + name + '&version=' + version;
    if (group) {
      url += '&group=' + group;
    }
    return url;
  }
}
