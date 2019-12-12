import { Injectable } from '@angular/core';
import {NodeInfo} from '../../entities/node-info';
import {AppConfig} from '../../config/app-config';
import {BaseService} from '../base.service';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/internal/Observable';
import {catchError, map} from 'rxjs/operators';
import {NodeType} from "../../enums/node-type.enum";
import {Feature} from "../../entities/feature";
import {LibraryService} from "../LibraryService/library.service";

@Injectable()
export class NodeService extends BaseService {
  private selected: NodeInfo;
  private maxNumberOfRunningTasksPerWorker = new Map();
  private maxNumberOfScheduledTasksPerWorker = new Map();
  private maxNumberOfTasksForNodesPerCluster: Map<string, Map<NodeType, number>> = new Map(new Map());

  constructor(appConfig: AppConfig, httpClient: HttpClient) {
    super(appConfig, httpClient);
  }

  hasMaxNumberOfRunningTasksForNode(nId: string): boolean {
    return this.maxNumberOfRunningTasksPerWorker.has(nId);
  }

  getMaxNumberOfRunningTasksForNode(nId: string): number {
    return this.maxNumberOfRunningTasksPerWorker.get(nId);
  }

  setMaxNumberOfRunningTasksForNode(nId: string, maxNumberOfTasks: number) {
    this.maxNumberOfRunningTasksPerWorker.set(nId, maxNumberOfTasks);
  }

  hasMaxNumberOfScheduledTasksForNode(nId: string): boolean {
    return this.maxNumberOfScheduledTasksPerWorker.has(nId);
  }

  getMaxNumberOfScheduledTasksForNode(nId: string): number {
    return this.maxNumberOfScheduledTasksPerWorker.get(nId);
  }

  setMaxNumberOfScheduledTasksForNode(nId: string, maxNumberOfTasks: number) {
    this.maxNumberOfScheduledTasksPerWorker.set(nId, maxNumberOfTasks);
  }


  /* MAX NUMBER OF TASKS FOR NODES OF CLUSTER */
  hasMaxNumberOfTasksForNodesOfCluster(clusterId: string, nodeType: NodeType): boolean {
    if (this.maxNumberOfTasksForNodesPerCluster.has(clusterId)) {
      let clusterValues = this.maxNumberOfTasksForNodesPerCluster.get(clusterId);
      return clusterValues.has(nodeType);
    }
    return false;
  }

  getMaxNumberOfTasksForNodesOfCluster(clusterId: string, nodeType: NodeType): number {
    return this.maxNumberOfTasksForNodesPerCluster.get(clusterId).get(nodeType);
  }

  setMaxNumberOfTasksForNodesOfCluster(clusterId: string, nodeType: NodeType, maxNumberOfTasks: number): void {
    if (!this.maxNumberOfTasksForNodesPerCluster.has(clusterId)) {
      this.maxNumberOfTasksForNodesPerCluster.set(clusterId, new Map());
    }
    this.maxNumberOfTasksForNodesPerCluster.get(clusterId).set(nodeType, maxNumberOfTasks);
  }


  calcMaxNumberOfTasksForNodesOfCluster(clusterId: string, nodes: NodeInfo[], nodeType: NodeType): number {
    let currMaxNumber = 0;
    if (this.hasMaxNumberOfTasksForNodesOfCluster(clusterId, nodeType)) {
      currMaxNumber = this.getMaxNumberOfTasksForNodesOfCluster(clusterId, nodeType);
    }

    const maxNumberOfTasksForGivenNodes = this.getMaxNumberOfTasksOfNodes(nodes);
    return this.calcMaxNodeValue(maxNumberOfTasksForGivenNodes, currMaxNumber);
  }

  calcMaxLoadForNodesOfCluster(nodes: NodeInfo[]): number {
    const maxLoadOfGivenNodes = this.getMaxLoadOfNodes(nodes);
    return this.calcMaxNodeValue(maxLoadOfGivenNodes, 0);
  }

  private calcMaxNodeValue(maxValueOfGivenNodes: number, currMaxValue: number): number {
    let newMaxValue = 0;

    if (maxValueOfGivenNodes === 0) {
      newMaxValue = 0;
    } else if (maxValueOfGivenNodes > currMaxValue) {
      newMaxValue = maxValueOfGivenNodes * 1.1;
    } else {
      newMaxValue = currMaxValue;
    }
    return newMaxValue;
  }

  private getMaxNumberOfTasksOfNodes(nodes: NodeInfo[]): number {
    let maxNumberOfTasks = 0;
    for (const node of nodes) {
      maxNumberOfTasks = Math.max(maxNumberOfTasks, node.numberOfTasksToFinish);
    }
    return maxNumberOfTasks;
  }

  private getMaxLoadOfNodes(nodes: NodeInfo[]): number {
    let maxLoad = 0;
    for (const node of nodes) {
      maxLoad = Math.max(maxLoad, node.load, node.numberOfCores);
    }
    return maxLoad;
  }

  getAllWorkersOfCluster(cId: string): Observable<NodeInfo[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingAllWorkers(cId);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => this.mapNodeInfos(json)),
      catchError(this.handleError<NodeInfo[]>('getAllWorkersOfCluster(cId)'))
    );
  }

  getAllReducersOfCluster(cId: string): Observable<NodeInfo[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingAllReducers(cId);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => this.mapNodeInfos(json)),
      catchError(this.handleError<NodeInfo[]>('getAllReducersOfCluster(cId)'))
    );
  }

  getNodeInfo(cId: string, nId: string): Observable<NodeInfo> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingNodeWithId(cId, nId);
    return this.httpClient.get<object>(url).pipe(
      map(json => this.mapNodeInfo(json)),
      catchError(this.handleError<NodeInfo>('getNodeInfo(cId, wId)'))
    );
  }

  private mapNodeInfos(values: object[]): NodeInfo[] {
    const nodes: NodeInfo[] = [];
    values.forEach(value => nodes.push(this.mapNodeInfo(value)));
    return nodes;
  }

  private mapNodeInfo(value: object): NodeInfo {
    return new NodeInfo(value);
  }

  getEnvironment(cId: string, nId: string): Observable<Feature[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingNodeEnvironment(cId, nId);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => LibraryService.mapFeatures(json)),
      catchError(this.handleError<Feature[]>('getEnvironment(nId)'))
    );
  }

  select(node: NodeInfo): void {
    this.selected = node;
  }

  getSelected(): NodeInfo {
    return this.selected;
  }
}
