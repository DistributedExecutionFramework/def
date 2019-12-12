import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClusterInfo} from '../../entities/cluster-info';
import {ClusterService} from '../../services/ClusterService/cluster.service';
import {ActivatedRoute, Router} from '@angular/router';
import {NodeService} from '../../services/NodeService/node.service';
import {Subscription} from 'rxjs/internal/Subscription';
import {interval} from 'rxjs/internal/observable/interval';
import {AppConfig} from '../../config/app-config';
import {Location} from '@angular/common';
import {Semaphore} from 'prex';
import {finalize} from 'rxjs/operators';
import {NavigationElement} from "../../routing/navigation-element";

@Component({
  selector: 'app-cluster-overview',
  templateUrl: './cluster-overview.component.html',
  styleUrls: ['./cluster-overview.component.css']
})
export class ClusterOverviewComponent implements OnInit, OnDestroy {
  private subscription: Subscription;
  private lock: Semaphore = new Semaphore(1);

  activeNavigationElement: NavigationElement;
  clusters: ClusterInfo[] = [];
  clustersLoading = true;
  showFeatures: Map<string, boolean>;
  featuresLoading: Map<string, boolean>;

  constructor(private clusterService: ClusterService,
              private workerService: NodeService,
              private router: Router,
              private route: ActivatedRoute,
              private appConfig: AppConfig,
              private location: Location) {
  }

  ngOnInit() {
    this.showFeatures = new Map<string, boolean>();
    this.featuresLoading = new Map<string, boolean>();
    this.setNavigationPath();

    // Initial fetch all clusters
    this.fetchClusters();

    // Setup periodic job and tasks update
    this.subscription = interval(this.appConfig.updateInterval).pipe()
      .subscribe(() => {
        const path = this.location.path();
        if (path === '/manager/clusters') {
          if (this.lock.count >= 0) {
            this.fetchClusters();
          }
        }
      });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  private async fetchClusters() {
    await this.lock.wait();
    this.clusterService.getAllClusters()
      .pipe(finalize(() => this.lock.release()))
      .subscribe(
        cluster => this.prepareClusters(cluster),
        error => console.error('Error while fetching Clusters: ' + error)
      );
  }

  private setNavigationPath() {
    this.activeNavigationElement = new NavigationElement(
      'Cluster Overview',
      './',
      '');
  }

  private prepareClusters(clusters: ClusterInfo[]) {
    for (const cluster of clusters) {
      this.calcTasks(cluster);

      if (!this.showFeatures.has(cluster.id)) {
        this.showFeatures.set(cluster.id, false);
        this.featuresLoading.set(cluster.id, true);
      }

      if (this.showFeatures.get(cluster.id)) {
        this.clusterService.getEnvironment(cluster.id)
          .pipe(finalize(() => this.featuresLoading.set(cluster.id, false)))
          .subscribe(
            features => cluster.setFeatures(features),
            error => console.error('Error while fetching Features from Cluster ' + cluster.id + ': ' + error)
          );
      }

      this.workerService.getAllWorkersOfCluster(cluster.id)
        .pipe(finalize(() => {
          this.clustersLoading = false;
          this.clusters = clusters;
        }))
        .subscribe(
          workers => {
            cluster.setWorkers(workers);
            this.calcTasks(cluster);
          },
          error => console.error('Error while fetch all workers from Cluster ' + cluster.id + ': ' + error)
        );
    }
  }

  private calcTasks(cluster: ClusterInfo) {
    let maxNumberOfTasks = 0;
    if (this.clusterService.hasMaxNumberOfTasksForCluster(cluster.id)) {
      maxNumberOfTasks = this.clusterService.getMaxNumberOfTasksForCluster(cluster.id);
    }
    if (cluster.numberOfTasksToFinish > maxNumberOfTasks) {
      maxNumberOfTasks = cluster.numberOfTasksToFinish;
    } else if (cluster.numberOfTasksToFinish === 0) {
      maxNumberOfTasks = 0;
    }
    this.clusterService.setMaxNumberOfTasksForCluster(cluster.id, maxNumberOfTasks);
  }

  openDetailsView(cluster: ClusterInfo): void {
    this.clusterService.select(cluster);
    this.router.navigate(['./' + cluster.id], { relativeTo: this.route});
  }

  getTasksPercent(cluster: ClusterInfo): number {
    if (this.clusterService.hasMaxNumberOfTasksForCluster(cluster.id)) {
      const maxNumberOfTasks = this.clusterService.getMaxNumberOfTasksForCluster(cluster.id);
      if (maxNumberOfTasks !== 0) {
        return cluster.numberOfTasksToFinish / maxNumberOfTasks * 100;
      }
    }
    return 0;
  }

  toggleShowFeatures(clusterId: string) {
    if (this.showFeatures.get(clusterId)) {
      this.showFeatures.set(clusterId, false);
    } else {
      this.showFeatures.set(clusterId, true);
      this.fetchClusters();
    }
  }
}
