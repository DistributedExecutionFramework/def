import {Injectable} from '@angular/core';
import {ClusterInfo} from '../../entities/cluster-info';
import {AppConfig} from '../../config/app-config';
import {BaseService} from '../base.service';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/internal/Observable';
import {catchError, map} from 'rxjs/operators';
import {NodeType} from "../../enums/node-type.enum";
import {Feature} from "../../entities/feature";
import {LibraryService} from "../LibraryService/library.service";

@Injectable()
export class ClusterService extends BaseService {
  private selected: ClusterInfo;
  private maxNumberOfTasksPerCluster = new Map();

  constructor(appConfig: AppConfig, httpClient: HttpClient) {
    super(appConfig, httpClient);
  }

  /* MAX NUMBER OF TASKS FOR CLUSTER */
  hasMaxNumberOfTasksForCluster(clusterId: string): boolean {
    return this.maxNumberOfTasksPerCluster.has(clusterId);
  }

  getMaxNumberOfTasksForCluster(clusterId: string): number {
    return this.maxNumberOfTasksPerCluster.get(clusterId);
  }

  setMaxNumberOfTasksForCluster(clusterId: string, maxNumberOfTasks: number): void {
    this.maxNumberOfTasksPerCluster.set(clusterId, maxNumberOfTasks);
  }

  getAllClusters(): Observable<ClusterInfo[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingAllClusters();
    return this.httpClient.get<object[]>(url).pipe(
      map(json => this.mapClusterInfos(json)),
      catchError(this.handleError<ClusterInfo[]>('getAllClusters()'))
    );
  }

  getClusterInfo(cId: string): Observable<ClusterInfo> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingClusterWithId(cId);
    return this.httpClient.get<object>(url).pipe(
      map(json => this.mapClusterInfo(json)),
      catchError(this.handleError<ClusterInfo>('getClusterInfo(cId)'))
    );
  }

  getEnvironment(cId: string): Observable<Feature[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingClusterEnvironment(cId);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => LibraryService.mapFeatures(json)),
      catchError(this.handleError<Feature[]>('getEnvironment(cId)'))
    );
  }

  private mapClusterInfos(values: object[]): ClusterInfo[] {
    const clusters: ClusterInfo[] = [];
    values.forEach(value => clusters.push(this.mapClusterInfo(value)));
    return clusters;
  }

  private mapClusterInfo(value: object): ClusterInfo {
    return new ClusterInfo(value);
  }

  select(cluster: ClusterInfo) {
    this.selected = cluster;
  }

  getSelected(): ClusterInfo {
    return this.selected;
  }
}
