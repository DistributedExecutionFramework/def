import {Component, OnDestroy, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {Subscription} from 'rxjs/internal/Subscription';
import {ActivatedRoute, Router} from '@angular/router';
import {TaskService} from '../../services/TaskService/task.service';
import {TimeService} from '../../services/TimeService/time.service';
import {DataConverterService} from '../../services/DataConverterService/data-converter.service';
import {NavigationElement} from '../../routing/navigation-element';
import {Task} from '../../entities/task';
import {Resource} from '../../entities/resource';
import {interval} from 'rxjs/internal/observable/interval';
import {AppConfig} from '../../config/app-config';
//import {Semaphore} from 'prex';
import {finalize} from 'rxjs/operators';
import {Mutex} from "async-mutex";

@Component({
  selector: 'app-task-details',
  templateUrl: './task-details.component.html',
  styleUrls: ['./task-details.component.css']
})
export class TaskDetailsComponent implements OnInit, OnDestroy {
  task: Task;
  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  inParametersLoading = true;
  inParametersToDisplay: Resource[] = [];
  outParametersLoading = true;
  outParametersToDisplay: Resource[] = [];

  scheduledTitleToDisplay = 'Task';
  schedulerSubtitleToDisplay = 'is scheduled';
  progressTitleToDisplay = 'Task';
  progressSubtitleToDisplay = 'is running';
  successTitleToDisplay = 'Successfully';
  successSubtitleToDisplay = 'finished';
  failureTitleToDisplay = 'Error';
  failureSubtitleToDisplay = 'occurred';


  private subscription: Subscription;
  private pId = '';
  private jId = '';
  private tId = '';
  private taskLock: Mutex = new Mutex();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private timeService: TimeService,
    private dataConverterService: DataConverterService,
    private location: Location,
    private appConfig: AppConfig
  ) { }

  ngOnInit(): void {
    this.pId = this.route.snapshot.paramMap.get('pId');
    this.jId = this.route.snapshot.paramMap.get('jId');
    this.tId = this.route.snapshot.paramMap.get('tId');

    this.setNavigationPath();

    // Initial fetch the task
    this.task = this.taskService.getSelected();
    if (this.task == null) {
      this.fetchTask();
    } else {
      this.updateInParameters();
      this.updateOutParameters();
    }

    // Setup periodic tasks update
    this.subscription = interval(this.appConfig.updateInterval).pipe()
      .subscribe(() => {
        const path = this.location.path();
        if (path === ('/manager/programs/' + this.pId + '/jobs/' + this.jId + '/tasks/' + this.tId)) {
          if (!this.taskLock.isLocked()) {
            this.fetchTask();
          }
        }
      });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private setNavigationPath() {
    this.navigationPath.push(new NavigationElement(
      'Program Overview',
      '../../../../../',
      '')
    );
    this.navigationPath.push(new NavigationElement(
      'Program Details',
      '../../../../',
      this.pId)
    );
    this.navigationPath.push(new NavigationElement(
      'Job Details',
      '../../',
      this.jId)
    );
    this.activeNavigationElement = new NavigationElement(
      'Task Details',
      './',
      this.tId
    );
  }

  private async fetchTask() {
    await this.taskLock.acquire();
    this.taskService.getTask(this.pId, this.jId, this.tId)
      .pipe(finalize(() => this.taskLock.release()))
      .subscribe(
        task => {
          if (task === null || task === undefined) {
            this.router.navigate(['../../'], { relativeTo: this.route});
          }
          this.prepareTask(task)
        },
        error => console.error('Error while fetch Task: ' + error)
      );
  }

  private prepareTask(task: Task): void {
    this.task = task;
    this.task.programId = this.pId;
    this.task.jobId = this.jId;
    this.task.id = this.tId;

    // let runtime = 0;
    // if (this.task.isFinished()) {
    //   runtime = this.timeService.getRuntimeOfFinishedTask(this.task);
    // } else {
    //   runtime = this.timeService.getCurrentRuntimeOfTask(this.task);
    // }
    // this.task.setRuntime(runtime);

    this.updateInParameters();
    this.updateOutParameters();
  }

  private updateInParameters(): void {
    if (this.inParametersToDisplay.length === 0) {
      this.task.inParameters.forEach((resource: Resource, key: string) => {
        this.dataConverterService.getNameOfDataType(resource.dataTypeId).subscribe(
          name => resource.dataTypeName = name
        );
        this.taskService.getDataValueOfTaskInputParameter(this.pId, this.jId, this.tId, key).subscribe(
          data => {
            resource.decodedData = data
          }
        );
        resource.displayName = key;
        this.inParametersToDisplay.push(resource);
      });
      if (this.inParametersToDisplay.length > 0) {
        this.inParametersLoading = false;
      }
    }
  }

  private updateOutParameters(): void {
    if (this.outParametersToDisplay.length === 0) {
      if (this.task.outParameters !== undefined && this.task.outParameters !== null) {
        this.task.outParameters.forEach((resource: Resource) => {
          this.dataConverterService.getNameOfDataType(resource.dataTypeId).subscribe(
            name => resource.dataTypeName = name
          );
          this.taskService.getDataValueOfTaskOutputParameter(this.pId, this.jId, this.tId, resource.id).subscribe(
            value => resource.decodedData = value
          );
          this.outParametersToDisplay.push(resource);
        });
      }
    }
    if (this.outParametersToDisplay.length > 0) {
      this.outParametersLoading = false;
    }
  }

  abortTask(): void {
    this.taskService.abortTask(this.task.programId, this.task.jobId, this.task.id).subscribe(
      state => {
        if (state === 'FAILED') {
          alert('Could not abort Task.');
        }
        this.fetchTask();
      },
      error => {
        console.log('Error while abort Task: ' + error);
      });
  }

  restartTask(): void {
    this.taskService.reRunTask(this.task.programId, this.task.jobId, this.task.id).subscribe(
      state => {
        if (state === 'FAILED') {
          alert('Could not restart Task.');
        }
        this.fetchTask();
      },
      error => {
        console.log('Error while restarting Task: ' + error);
      }
    );
  }
}
