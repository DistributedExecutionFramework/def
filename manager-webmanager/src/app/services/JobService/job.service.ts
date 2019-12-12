import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

import {catchError, map} from 'rxjs/operators';

import {Job} from '../../entities/job';
import {AppConfig} from '../../config/app-config';
import {BaseService} from '../base.service';
import {Observable} from 'rxjs/internal/Observable';

@Injectable()
export class JobService extends BaseService {

  private selected: Job;

  constructor(appConfig: AppConfig, httpClient: HttpClient) {
    super(appConfig, httpClient);
  }

  getAllJobsOfProgram(pId: string): Observable<Job[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingAllJobs(pId);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => this.mapJobs(json)),
      catchError(this.handleError<Job[]>('getAllJobsOfProgram(pId)'))
    );
  }

  getNrOfFinishedJobsOfProgram(pId: string): Observable<number> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingNrOfFinishedJobsOfProgramWithId(pId);
    return this.httpClient.get<number>(url).pipe(
      catchError(this.handleError<number>('getNrOfFinishedJobsOfProgram'))
    );
  }

  private mapJobs(values: object[]): Job[] {
    const jobs: Job[] = [];
    values.forEach(value => jobs.push(this.mapJob(value)));
    return jobs;
  }

  private mapJob(value: object): Job {
    return new Job(value);
  }

  select(job: Job) {
    this.selected = job;
  }

  getSelected(): Job {
    return this.selected;
  }

  getJob(pId: string, jId: string): Observable<Job> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingJobWithId(pId, jId);
    return this.httpClient.get<object>(url).pipe(
      map(json => this.mapJob(json)),
      catchError(this.handleError<Job>('getJob(pId, jId)'))
    );
  }

  abortJob(pId: string, jId: string): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForAbortingJobWithId(pId, jId);
    return this.httpClient.put<string>(url, '').pipe(
      catchError(this.handleError<string>('abortJob(pId, jId)'))
    );
  }

  getDataValueOfJobReducedResult(pId: string, jId: string, reducedResultId: string): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingDataValueOfJobReducedResult(pId, jId, reducedResultId);
    return this.httpClient.get<string>(url).pipe(
      catchError(this.handleError<string>('getDataValueOfJobReducedResult(pId, jId, reducedResultId)'))
    );
  }

}
