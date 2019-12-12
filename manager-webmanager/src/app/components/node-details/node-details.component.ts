import {Component, OnDestroy, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {NavigationElement} from '../../routing/navigation-element';
import {NodeInfo} from '../../entities/node-info';
import {ActivatedRoute} from '@angular/router';
import {NodeService} from '../../services/NodeService/node.service';
import {AppConfig} from '../../config/app-config';
import {Subscription} from 'rxjs/internal/Subscription';
import {Semaphore} from 'prex';
import {interval} from 'rxjs/internal/observable/interval';
import {finalize} from 'rxjs/operators';
import {NodeType} from "../../enums/node-type.enum";
import {Feature} from "../../entities/feature";

@Component({
  selector: 'app-node-details',
  templateUrl: './node-details.component.html',
  styleUrls: ['./node-details.component.css']
})
export class NodeDetailsComponent implements OnInit, OnDestroy {
  private nId: string;
  private cId: string;
  private subscription: Subscription;
  private lock: Semaphore = new Semaphore(2);

  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  node: NodeInfo;
  hasTasks = false;
  maxLoad: number = 0;
  features: Feature[] = [];

  constructor(
    private route: ActivatedRoute,
    private nodeService: NodeService,
    private location: Location,
    private appConfig: AppConfig,
  ) { }

  ngOnInit() {
    this.cId = this.route.snapshot.paramMap.get('cId');
    this.nId = this.route.snapshot.paramMap.get('nId');

    // Update navigation path
    this.setNavigationPath();

    // Initial fetching of node
    this.node = this.nodeService.getSelected();
    if (this.node == null) {
      this.fetchNode();
    } else {
      this.prepareNode(this.node);
      this.features = this.node.features;
    }

    this.subscription = interval(this.appConfig.updateInterval).pipe()
      .subscribe(() => {
        const path = this.location.path();
        if (path === ('/manager/clusters/' + this.cId + '/nodes/' + this.nId)) {
          if (this.lock.count > 1) {
            this.fetchNode();
          }
        }
      });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }


  private prepareNode(node: NodeInfo): void {
    if (node !== undefined) {
      this.node = node;

      let maxNumberOfRunningTasks = 0;
      let maxNumberOfScheduledTasks = 0;

      if (this.nodeService.hasMaxNumberOfRunningTasksForNode(node.id)) {
        maxNumberOfRunningTasks = this.nodeService.getMaxNumberOfRunningTasksForNode(node.id);
      }
      if (this.nodeService.hasMaxNumberOfScheduledTasksForNode(node.id)) {
        maxNumberOfScheduledTasks = this.nodeService.getMaxNumberOfScheduledTasksForNode(node.id);
      }

      if (this.node.numberOfRunningTasks > maxNumberOfRunningTasks) {
        maxNumberOfRunningTasks = this.node.numberOfRunningTasks;
      } else if (this.node.numberOfRunningTasks === 0) {
        maxNumberOfRunningTasks = 0;
      }
      this.nodeService.setMaxNumberOfRunningTasksForNode(this.node.id, maxNumberOfRunningTasks);

      if (this.node.numberOfQueuedTasks > maxNumberOfScheduledTasks) {
        maxNumberOfScheduledTasks = this.node.numberOfQueuedTasks;
      } else if (this.node.numberOfQueuedTasks === 0) {
        maxNumberOfScheduledTasks = 0;
      }
      this.nodeService.setMaxNumberOfScheduledTasksForNode(this.node.id, maxNumberOfScheduledTasks);

      if ((this.node.numberOfRunningTasks + this.node.numberOfQueuedTasks) > 0) {
        this.hasTasks = true;
      }

      if (this.node.load > this.node.numberOfCores) {
        this.maxLoad = this.node.load;
      } else {
        this.maxLoad = this.node.numberOfCores;
      }
      this.maxLoad *= 1.1;
    }
  }

  private setNavigationPath(): void {
    this.navigationPath.push(
      new NavigationElement(
        'Cluster Overview',
        '../../../',
        '')
    );
    this.navigationPath.push(
      new NavigationElement(
        'Cluster Details',
        '../../',
        this.cId)
    );
    this.activeNavigationElement = new NavigationElement(
      'Node Details',
      './',
      this.nId);
  }

  private async fetchNode(): Promise<void> {
    await this.lock.wait();
    await this.lock.wait(); // wait two times, because of two releases

    this.nodeService.getNodeInfo(this.cId, this.nId)
      .pipe(finalize(() => this.lock.release()))
      .subscribe(
        worker => this.prepareNode(worker),
        error => console.error('Failed to fetch node: ' + error)
      );
    this.nodeService.getEnvironment(this.cId, this.nId)
      .pipe(finalize(() => this.lock.release()))
      .subscribe(
        features => this.features = features,
        error => console.error('Failed to fetch features: ' + error)
      )
  }

  getRunningTasksPercent(): number {
    if (this.nodeService.hasMaxNumberOfRunningTasksForNode(this.node.id)) {
      const maxNumberOfTasks = this.nodeService.getMaxNumberOfRunningTasksForNode(this.node.id);
      if (maxNumberOfTasks !== 0) {
        return this.node.numberOfRunningTasks / maxNumberOfTasks * 100;
      }
    }
    return 0;
  }


  getScheduledTasksPercent(): number {
    if (this.nodeService.hasMaxNumberOfScheduledTasksForNode(this.node.id)) {
      const maxNumberOfTasks = this.nodeService.getMaxNumberOfScheduledTasksForNode(this.node.id);
      if (maxNumberOfTasks !== 0) {
        return this.node.numberOfQueuedTasks / maxNumberOfTasks * 100;
      }
    }
    return 0;
  }

  getLoadPercent(): number {
    return this.calcPercent(this.node.load, this.maxLoad);
  }

  getNumberOfCoresPercent(): number {
    return this.calcPercent(this.node.numberOfCores, this.maxLoad);
  }

  private calcPercent(fraction: number, maxValue: number): number {
    if (maxValue !== 0) {
      return fraction / maxValue * 100;
    }
    return 0;
  }
}
