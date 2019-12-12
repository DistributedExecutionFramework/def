import {Injectable} from '@angular/core';
import {Task} from '../../entities/task';
import {AppConfig} from '../../config/app-config';
import {BaseService} from '../base.service';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs/internal/Observable';
import {catchError, map} from 'rxjs/operators';
import {SortingCriterion} from "../../enums/sorting-criterion.enum";

@Injectable()
export class TaskService extends BaseService {
  private selected: Task;

  constructor(appConfig: AppConfig, httpClient: HttpClient) {
    super(appConfig, httpClient);
  }

  private mapTasks(values: object[]): Task[] {
    const tasks: Task[] = [];
    values.forEach(value => tasks.push(this.mapTask(value)));
    return tasks;
  }

  private mapTask(value: object): Task {
    return new Task(value);
  }

  select(task: Task): void {
    this.selected = task;
  }

  getSelected(): Task {
    return this.selected;
  }

  getSortedTasksWithState(pId: string, jId: string, state: string, sortingCriterion: SortingCriterion, nrOfTasks: number): Observable<Task[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingTasksWithState(pId, jId, state, sortingCriterion, nrOfTasks);
    console.log(url);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => this.mapTasks(json)),
      catchError(this.handleError<Task[]>('getSortedTasksWithState(pId, jId, state, sortingCriterion, nrOfTasks)'))
    );
  }

  getTasksWithFilters(pId: string, jId: string, filters: string[], sortingCriterion: SortingCriterion): Observable <Task[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingTasksWithFilter(pId, jId, sortingCriterion);
    let params = new HttpParams();
    params = params.append("filters", filters.join(','));

    return this.httpClient.get<object[]>(url, { params: params }).pipe(
      map(json => this.mapTasks(json)),
      catchError(this.handleError<Task[]>('getTasksWithFilters(pId, jId)'))
    );
  }

  getTask(pId: string, jId: string, tId: string): Observable<Task> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingTaskWithId(pId, jId, tId);
    return this.httpClient.get<object>(url).pipe(
      map(json => this.mapTask(json)),
      catchError(this.handleError<Task>('getTask(pId, jId, tId)'))
    );
  }

  abortTask(pId: string, jId: string, tId: string): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForAbortingTaskWithId(pId, jId, tId);
    return this.httpClient.put<string>(url, '').pipe(
      catchError(this.handleError<string>('abortTask(pId, jId, tId)'))
    );
  }

  public getDataValueOfTaskInputParameter(pId: string, jId: string, tId: string, inParamName: string): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingDataValueOfTaskInputParameter(pId, jId, tId, inParamName);
    return this.httpClient.get(url, {responseType: 'text'}).pipe(
      catchError(this.handleError<string>('getDataValueOfTaskInputParameter(pId, jId, tId, inParamName)'))
    );
  }

  public getDataValueOfTaskOutputParameter(pId: string, jId: string, tId: string, outParamId: string): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingDataValueOfTaskOutputParameter(pId, jId, tId, outParamId);
    return this.httpClient.get(url, {responseType: 'text'}).pipe(
      catchError(this.handleError<string>('getDataValueOfTaskOutputParameter(pId, jId, tId, outParamId)'))
    );
  }

  reRunTask(pId: string, jId: string, tId: string): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForReRunTaskWithId(pId, jId, tId);
    return this.httpClient.put<string>(url, '').pipe(
      catchError(this.handleError<string>('reRunTask(pId, jId, tId, outParamId)'))
    );
  }
}
