import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClusterInfo} from '../../entities/cluster-info';
import {NodeInfo} from '../../entities/node-info';
import {NavigationElement} from '../../routing/navigation-element';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {Subscription} from 'rxjs/internal/Subscription';
import {ClusterService} from '../../services/ClusterService/cluster.service';
import {NodeService} from '../../services/NodeService/node.service';
import {interval} from 'rxjs/internal/observable/interval';
import {Semaphore} from 'prex';
import {finalize} from 'rxjs/operators';
import {AppConfig} from '../../config/app-config';
import {NodeType} from '../../enums/node-type.enum';

@Component({
  selector: 'app-cluster-details',
  templateUrl: './cluster-details.component.html',
  styleUrls: ['./cluster-details.component.css']
})
export class ClusterDetailsComponent implements OnInit, OnDestroy {
  private subscription: Subscription;
  private cId: string;
  private lock: Semaphore = new Semaphore(1);
  private maxLoadsPerNodeType: Map<NodeType, number> = new Map();

  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  cluster: ClusterInfo;
  workers: NodeInfo[];
  reducers: NodeInfo[];
  clientRoutineWorkers: NodeInfo[];
  clusterLoading = true;
  workersLoading = true;
  reducersLoading = true;
  clientRoutineWorkersLoading = true;
  featuresLoading = true;
  workerType = NodeType.WORKER;
  reducerType = NodeType.REDUCER;
  showNodeFeatures: Map<string, boolean>;
  nodeFeaturesLoading: Map<string, boolean>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private clusterService: ClusterService,
    private nodeService: NodeService,
    private appConfig: AppConfig
  ) { }

  ngOnInit(): void {
    this.cId = this.route.snapshot.paramMap.get('cId');
    this.maxLoadsPerNodeType.set(NodeType.WORKER, 0);
    this.maxLoadsPerNodeType.set(NodeType.REDUCER, 0);
    this.showNodeFeatures = new Map<string, boolean>();
    this.nodeFeaturesLoading = new Map<string, boolean>();
    this.workers = [];
    this.reducers = [];
    this.clientRoutineWorkers = [];

    // Update navigation path
    this.setNavigationPath();

    // Initial fetching of cluster
    this.cluster = this.clusterService.getSelected();
    if (this.cluster == null) {
      this.fetchCluster();
    } else {
      if (this.cluster.workers && this.cluster.workers.length > 0) {
        this.prepareWorkers(this.cluster, this.cluster.workers);
        this.workersLoading = false;
      }
      if (this.cluster.reducers && this.cluster.reducers.length > 0) {
        this.prepareReducers(this.cluster, this.cluster.reducers);
        this.reducersLoading = false;
      }
      if (this.cluster.clientRoutineWorkers && this.cluster.clientRoutineWorkers.length > 0) {
        this.prepareClientRoutineWorkers(this.cluster, this.cluster.clientRoutineWorkers);
        this.clientRoutineWorkersLoading = false;
      }
      this.clusterLoading = false;
      this.featuresLoading = false;
    }

    // Subscribe to fetch cluster info and worker periodically
    this.subscription = interval(this.appConfig.updateInterval).pipe()
      .subscribe(() => {
        if (this.lock.count > 0) {
          // this site is updated all the time (even when the user isn't on this page)
          // to ensure that the tasks peak is always correct
          this.fetchCluster();
        }
      });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  setNavigationPath(): void {
    this.navigationPath.push(
      new NavigationElement(
        'Cluster Overview',
        '../',
        '')
    );
    this.activeNavigationElement = new NavigationElement(
      'Cluster Details',
      './',
      this.cId);
  }

  private async prepareCluster(cluster: ClusterInfo): Promise<void> {

    const lock = new Semaphore(0);

    this.clusterService.getEnvironment(cluster.id)
      .pipe(finalize(() => {
        lock.release();
        this.featuresLoading = false;
      }))
      .subscribe(
        features => cluster.setFeatures(features),
        error => console.error('Error while fething features: ' + error)
      );

    // Fetch all workers and update data
    this.nodeService.getAllWorkersOfCluster(cluster.id)
      .pipe(finalize(() => {
        lock.release();
        this.workersLoading = false;
      }))
      .subscribe(
        workers => this.prepareWorkers(cluster, workers),
        error => console.error('Error while fetching workers: ' + error)
      );

    // Fetch all reducers and update data
    this.nodeService.getAllReducersOfCluster(cluster.id)
      .pipe(finalize(() => {
        lock.release();
        this.reducersLoading = false;
      }))
      .subscribe(
        reducers => this.prepareReducers(cluster, reducers),
        error => console.error('Error while fetching reducers: ' + error)
      );

    // Fetch all client routine workers and update data
    this.nodeService.getAllClientRoutineWorkersOfCluster(cluster.id)
      .pipe(finalize(() => {
        lock.release();
        this.clientRoutineWorkersLoading = false;
      }))
      .subscribe(
        clientRoutineWorkers => this.prepareClientRoutineWorkers(cluster, clientRoutineWorkers),
        error => console.error('Error while fetching client routine workers: ' + error)
      );

    // Wait for 3 releases
    for (let i = 0; i < 3; i++) {
      await lock.wait();
    }

    cluster.adaptCalculatedValues(this.cluster);
    this.cluster = cluster;
  }

  private prepareWorkers(cluster: ClusterInfo, workers: NodeInfo[]): void {
    // Update workers of cluster
    cluster.setWorkers(workers);
    this.prepareNodes(cluster, workers, NodeType.WORKER);
    this.workers = workers;
  }

  private prepareReducers(cluster: ClusterInfo, reducers: NodeInfo[]): void {
    // Update reducers of cluster
    if (!reducers) {
      reducers = [];
    }
    cluster.setReducers(reducers);
    this.prepareNodes(cluster, reducers, NodeType.REDUCER);
    this.reducers = reducers;
  }

  private prepareClientRoutineWorkers(cluster: ClusterInfo, clientRoutineWorkers: NodeInfo[]): void {
    // Udpate client routine workers of cluster
    if (!clientRoutineWorkers) {
      clientRoutineWorkers = [];
    }
    cluster.setClientRoutineWorkers(clientRoutineWorkers);
    this.prepareNodes(cluster, clientRoutineWorkers, NodeType.CLIENT_ROUTINE_WORKER);
    this.clientRoutineWorkers = clientRoutineWorkers;
  }

  private prepareNodes(cluster: ClusterInfo, nodes: NodeInfo[], nodeType: NodeType): void {
    // calc max number of tasks for nodes
    const maxNumberOfTasks = this.nodeService.calcMaxNumberOfTasksForNodesOfCluster(cluster.id, nodes, nodeType);
    this.nodeService.setMaxNumberOfTasksForNodesOfCluster(cluster.id, nodeType, maxNumberOfTasks);

    // calc max load for nodes
    const maxLoad = this.nodeService.calcMaxLoadForNodesOfCluster(nodes);
    this.maxLoadsPerNodeType.set(nodeType, maxLoad);

    // add existing features
    const existingNodes = this.workers.concat(this.reducers).concat(this.clientRoutineWorkers);
    for (let node of existingNodes) {
      for (let newNode of nodes) {
        if (node.id === newNode.id) {
          newNode.features = node.features;
          break;
        }
      }
    }

    nodes.forEach(node => {
      if (!this.showNodeFeatures.has(node.id)) {
        this.showNodeFeatures.set(node.id, false);
        this.nodeFeaturesLoading.set(node.id, true);
      }
    });
  }

  private async fetchCluster(): Promise<void> {
    await this.lock.wait();
    this.clusterService.getClusterInfo(this.cId)
      .pipe(finalize(() => {
        this.lock.release();
        this.clusterLoading = false;
      }))
      .subscribe(
        cluster => this.prepareCluster(cluster),
        error => console.error('Error while fetch cluster: ' + error)
      );
  }

  getTasksPercentForNode(node: NodeInfo, nodeType: NodeType): number {
    let maxNumberOfTasks = 0;
    if (this.nodeService.hasMaxNumberOfTasksForNodesOfCluster(this.cluster.id, nodeType)) {
      maxNumberOfTasks = this.nodeService.getMaxNumberOfTasksForNodesOfCluster(this.cluster.id, nodeType);
    }
    return this.calcPercent(node.numberOfElementsToFinish, maxNumberOfTasks);
  }

  getLoadPercentForNode(node: NodeInfo, nodeType: NodeType): number {
    return this.calcPercent(node.load, this.maxLoadsPerNodeType.get(nodeType));
  }

  getNumberOfCoresPercentForNode(node: NodeInfo, nodeType: NodeType): number {
    return this.calcPercent(node.numberOfCores, this.maxLoadsPerNodeType.get(nodeType));
  }

  private calcPercent(fraction: number, maxValue: number): number {
    if (maxValue !== 0) {
      return fraction / maxValue * 100;
    }
    return 0;
  }

  openDetailsView(node: NodeInfo) {
    this.fetchNodeFeatures(node);
    this.nodeService.select(node);
    this.router.navigate(['./nodes/' + node.id], { relativeTo: this.route});
  }

  toggleShowNodeFeatures(node: NodeInfo) {
    if (this.showNodeFeatures.get(node.id)) {
      this.showNodeFeatures.set(node.id, false);
    } else {
      this.showNodeFeatures.set(node.id, true);
      if (node.features.length === 0) {
        this.fetchNodeFeatures(node);
      }
    }
  }

  private fetchNodeFeatures(node: NodeInfo): void {
    this.nodeService.getEnvironment(this.cluster.id, node.id)
      .pipe(finalize(() => this.nodeFeaturesLoading.set(node.id, false)))
      .subscribe(
        features => node.features = features,
        error => console.error('Error while fetching Node environment: ' + error)
      );
  }
}
