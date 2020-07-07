import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs/internal/Subscription';
import {Job} from '../../entities/job';
import {Task} from '../../entities/task';
import {NavigationElement} from '../../routing/navigation-element';
import {Resource} from '../../entities/resource';
import {ActivatedRoute, Router} from '@angular/router';
import {JobService} from '../../services/JobService/job.service';
import {TimeService} from '../../services/TimeService/time.service';
import {Location} from '@angular/common';
import {interval} from 'rxjs/internal/observable/interval';
import {AppConfig} from '../../config/app-config';
import {DataConverterService} from '../../services/DataConverterService/data-converter.service';
import {TaskService} from '../../services/TaskService/task.service';
//import {Semaphore} from 'prex';
import {finalize} from 'rxjs/operators';
import {SortingCriterion} from "../../enums/sorting-criterion.enum";
import { Mutex } from 'async-mutex';

@Component({
  selector: 'app-job-details',
  templateUrl: './job-details.component.html',
  styleUrls: ['./job-details.component.css']
})
export class JobDetailsComponent implements OnInit, OnDestroy {
  job: Job;
  tasks: Task[] = [];
  percent = 0;
  oldPercentage = 0;
  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  outParametersToDisplay: Resource[] = [];
  progressTextToDisplay = 'Tasks completed';
  successTitleToDisplay = 'Successfully';
  successSubtitleToDisplay = 'completed';
  failureTitleToDisplay = 'Error';
  failureSubtitleToDisplay = 'occurred';
  animateProgress = true;
  tasksLoading = true;
  filterTaskState = 'FAILED';
  filterChangedByUser = false;
  filterTaskCount = 10;
  currentJobRuntime: number = 0;
  taskFilters: string[] = [];
  displayedTaskFilter: string = '';
  private subscription: Subscription;
  private pId = '';
  private jId = '';
  private tasksLock: Mutex = new Mutex();
  private jobLock: Mutex = new Mutex();

  currentSortingCriterion: SortingCriterion = SortingCriterion.NO_SORTING;
  creationDateNewestType: SortingCriterion = SortingCriterion.CREATION_DATE_FROM_NEWEST;
  creationDateOldestType: SortingCriterion = SortingCriterion.CREATION_DATE_FROM_OLDEST;
  startDateNewestType: SortingCriterion = SortingCriterion.START_DATE_FROM_NEWEST;
  startDateOldestType: SortingCriterion = SortingCriterion.START_DATE_FROM_OLDEST;
  noSortingType: SortingCriterion = SortingCriterion.NO_SORTING;

  constructor(
    private route: ActivatedRoute,
    private jobService: JobService,
    private taskService: TaskService,
    public timeService: TimeService,
    private dataConverterService: DataConverterService,
    private location: Location,
    private router: Router,
    private appConfig: AppConfig
  ) {
  }

  ngOnInit() {
    this.pId = this.route.snapshot.paramMap.get('pId');
    this.jId = this.route.snapshot.paramMap.get('jId');

    this.setNavigationPath();

    // Initial fetch job
    this.job = this.jobService.getSelected();
    if (this.job == undefined || this.job == null) {
      this.fetchJob();
    } else {
      this.prepareJob(this.job);

      if (this.tasks == undefined || this.tasks.length === 0) {
        this.fetchTasks();
      }
    }

    // Setup periodic job and tasks update
    this.subscription = interval(this.appConfig.updateInterval).pipe()
      .subscribe(() => {
        const path = this.location.path();
        if (path === ('/manager/programs/' + this.pId + '/jobs/' + this.jId)) {
          if (!this.jobLock.isLocked()) {
            this.fetchJob();
          }
        }
      });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  changedTaskFilter() {
    this.filterChangedByUser = true;
    this.tasksLoading = true;
    this.fetchTasks();
  }

  openDetailsView(task: Task) {
    this.taskService.select(task);
    this.router.navigate(['./tasks/' + task.id], { relativeTo: this.route});
  }

  private setNavigationPath() {
    this.navigationPath.push(new NavigationElement(
      'Program Overview',
      '../../../',
      '')
    );
    this.navigationPath.push(new NavigationElement(
      'Program Details',
      '../../',
      this.pId)
    );
    this.activeNavigationElement = new NavigationElement(
      'Job Details',
      './',
      this.jId);
  }

  private calculateProgressInPercent(): void {
    if (this.job.getNrOfTasks() > 0) {
      this.percent = (this.job.getNrOfFinishedTasks() / this.job.getNrOfTasks()) * 100;
    } else {
      this.percent = 0;
    }
  }

  private async fetchJob() {
    await this.jobLock.acquire();
    this.jobService.getJob(this.pId, this.jId)
      .pipe(finalize(() => this.jobLock.release()))
      .subscribe(
        job => {
          if (job === null || job === undefined) {
            this.router.navigate(['../../'], { relativeTo: this.route});
          }
          this.prepareJob(job);
          if (this.areFiltersActive()) {
            this.fetchTasksWithFilter();
          } else {
            this.fetchTasks();
          }
        },
        error => console.error('Error while fetch Job: ' + error)
      );
  }

  private prepareJob(job: Job) {
    this.job = job;
    this.oldPercentage = this.percent;
    this.calculateProgressInPercent();
    if (this.oldPercentage === 0 && this.percent > 0) {
      this.animateProgress = true;
    } else {
      this.animateProgress = false;
    }
    let runtime = 0;
    if (this.job.isFinished() && this.job.getNrOfFinishedTasks() === this.job.getNrOfTasks()) {
      runtime = this.timeService.getRuntimeOfFinishedJob(this.job);
    } else {
      runtime = this.timeService.getCurrentRuntimeOfJob(this.job);
    }
    this.job.setRuntime(runtime);
    this.currentJobRuntime = this.job.runtime;

    if (this.outParametersToDisplay === undefined || this.outParametersToDisplay === null || this.outParametersToDisplay.length === 0) {
      this.outParametersToDisplay = [];
      if (this.job.reducedResults !== undefined && this.job.reducedResults !== null) {
        this.job.reducedResults.forEach((entry: Resource) => {
          this.outParametersToDisplay.push(entry);
          this.dataConverterService.getNameOfDataType(entry.dataTypeId).subscribe(
            name => entry.dataTypeName = name
          );
          this.jobService.getDataValueOfJobReducedResult(this.pId, this.jId, entry.id).subscribe(
            value => entry.decodedData = value
          );
        });
      }
    }
    if (!this.filterChangedByUser) {
      this.filterTaskState = this.job.state;
    }
  }

  private async fetchTasks() {
    await this.tasksLock.acquire();
    await this.jobLock.acquire();
    this.taskService.getSortedTasksWithState(this.pId, this.jId, this.filterTaskState, this.currentSortingCriterion, this.filterTaskCount)
      .pipe(finalize(() => {
        this.tasksLoading = false;
        this.jobLock.release();
        this.tasksLock.release();
      }))
      .subscribe(
        tasks => this.prepareTasks(tasks),
        error => console.error('Error while fetching tasks: ' + error)
      );
  }

  private async fetchTasksWithFilter() {
    await this.tasksLock.acquire();
    await this.jobLock.acquire();

    this.taskService.getTasksWithFilters(this.pId, this.jId, this.taskFilters, this.currentSortingCriterion)
      .pipe(finalize(() => {
        this.tasksLoading = false;
        this.jobLock.release();
        this.tasksLock.release();
      }))
      .subscribe(tasks => this.prepareTasks(tasks),
        error => console.error('Error while fetching tasks with filter: ' + error)
      );
  }

  private prepareTasks(tasks: Task[]): void {
    // const tmpTasks: Task[] = [];
    // for (const task of tasks) {
    //   let taskRuntime = 0;
    //   if (task.isFinished()) {
    //     taskRuntime = this.timeService.getRuntimeOfFinishedTask(task);
    //   } else {
    //     taskRuntime = this.timeService.getCurrentRuntimeOfTask(task);
    //   }
    //   task.setRuntime(taskRuntime);
    //   tmpTasks.push(task);
    // }
    // this.tasks = tmpTasks;
    this.tasks = tasks;
  }

  abortJob(): void {
    this.jobService.abortJob(this.pId, this.jId).subscribe(
      state => {
        if (state === 'FAILED') {
          alert('Could not abort Job.');
        }
        this.fetchJob();
      },
      error => {
        console.log('Error while aborting Job: ' + error);
      }
    );
  }

  areFiltersActive(): boolean {
    if (this.taskFilters.length > 0) {
      return true;
    }
    return false;
  }

  addFilter(): void {
    if (this.displayedTaskFilter !== '') {
      this.taskFilters.push(this.displayedTaskFilter);
    }
    this.displayedTaskFilter = '';
    if (this.areFiltersActive()) {
      this.removeSortingCriterion();
      this.fetchTasksWithFilter();
    } else {
      this.fetchTasks();
    }
  }

  deleteFilter(filter: string): void {
    const index = this.taskFilters.indexOf(filter);
    if (index !== -1) {
      this.taskFilters.splice(index, 1);
    }
    if (this.areFiltersActive()) {
      this.fetchTasksWithFilter();
    } else {
      this.fetchTasks();
    }
  }

  sortTasks(sortingCriterion: SortingCriterion) {
    this.currentSortingCriterion = sortingCriterion;
    this.tasksLoading = true;
    this.fetchTasks();
  }

  removeSortingCriterion(): void {
    this.currentSortingCriterion = SortingCriterion.NO_SORTING;
  }
}
