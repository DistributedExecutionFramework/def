import {Component, OnDestroy, OnInit} from '@angular/core';
import {Program} from '../../entities/program';
import {Job} from '../../entities/job';
import {ActivatedRoute, Router} from '@angular/router';
import {NavigationElement} from '../../routing/navigation-element';
import {ProgramService} from '../../services/ProgramService/program.service';
import {JobService} from '../../services/JobService/job.service';
import {TimeService} from '../../services/TimeService/time.service';
import {interval} from 'rxjs/internal/observable/interval';
import {Location} from '@angular/common';
import {AppConfig} from '../../config/app-config';
import {Subscription} from 'rxjs/internal/Subscription';
import {Semaphore} from 'prex';
import {finalize} from 'rxjs/operators';

@Component({
  selector: 'app-program-details',
  templateUrl: './program-details.component.html',
  styleUrls: ['./program-details.component.css']
})
export class ProgramDetailsComponent implements OnInit, OnDestroy {

  pId: string;
  program: Program;
  jobs: Job[] = [];
  jobsLoading = true;

  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  percent = 0;
  oldPercentage = 0;
  progressTextToDisplay = 'Tasks completed';
  successTitleToDisplay = 'Successfully';
  successSubtitleToDisplay = 'completed';
  failureTitleToDisplay = 'Error';
  failureSubtitleToDisplay = 'occurred';
  animateProgress = true;
  editingName = false;
  editingDescription = false;
  newProgramName = '';
  newProgramDescription = '';
  currentRuntime: number = 0;
  private subscription: Subscription;
  private programLock: Semaphore = new Semaphore(1);
  private jobsLock: Semaphore = new Semaphore(1);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private programService: ProgramService,
    private jobService: JobService,
    public timeService: TimeService,
    private location: Location,
    private appConfig: AppConfig
  ) {
  }

  ngOnInit() {
    this.pId = this.route.snapshot.paramMap.get('pId');

    this.setNavigationPath();
    // Fetch program

    this.program = this.programService.getSelected();
    if (this.program == null || this.program == undefined) {
      this.fetchProgram(this.pId);
    } else {
      if (this.jobs == undefined || this.jobs.length === 0) {
        this.fetchAllJobsOfProgram();
      } else {
        this.prepareProgram(this.program);
      }
    }

    // Setup periodic program update
    this.subscription = interval(this.appConfig.updateInterval).pipe()
      .subscribe(() => {
        const path = this.location.path();
        if (path === ('/manager/programs/' + this.pId) && !this.editingName && !this.editingDescription) {
          if (this.programLock.count >= 0) {
            this.fetchProgram(this.pId);
          }
        }
      });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  private async fetchProgram(pId: string): Promise<void> {
    await this.programLock.wait();
    this.programService.getProgram(pId)
      .pipe(finalize(() => this.programLock.release()))
      .subscribe(
        program => {
          if (program === null || program === undefined) {
            this.router.navigate(['../'], { relativeTo: this.route});
          }
          this.prepareProgram(program);
          this.fetchAllJobsOfProgram();
        },
        error => {
          console.error('Error while fetch program: ' + error);
          this.router.navigate(['../'], { relativeTo: this.route});
        }
      );
  }

  private prepareProgram(program: Program) {
    if (program !== undefined) {
      this.program = program;
      let runtime = 0;
      if (this.program.isFinished()) {
        runtime = this.timeService.getRuntimeOfFinishedProgram(this.program);
      } else {
        runtime = this.timeService.getCurrentRuntimeOfProgram(this.program);
      }
      this.program.setRuntime(runtime);
      this.currentRuntime = this.program.runtime;
      this.oldPercentage = this.percent;
      this.calculateProgressInPercent();
      if (this.oldPercentage === 0 && this.percent > 0) {
        this.animateProgress = true;
      } else {
        this.animateProgress = false;
      }
      this.newProgramName = this.program.name;
      this.newProgramDescription = this.program.description;
    }
  }

  setNavigationPath(): void {
    this.navigationPath.push(new NavigationElement(
      'Program Overview',
      '../',
      ''));
    this.activeNavigationElement = new NavigationElement(
      'Program Details',
      './',
      this.pId);
  }

  private async fetchAllJobsOfProgram(): Promise<void> {
    await this.jobsLock.wait();
    this.jobService.getAllJobsOfProgram(this.program.id)
      .pipe(finalize(() => {
        this.jobsLock.release();
        this.jobsLoading = false;
      }))
      .subscribe(
        jobs => {
          const tmpJobs = [];
          for (const job of jobs) {
            let jobRuntime = 0;
            if (job.isFinished()) {
              jobRuntime = this.timeService.getRuntimeOfFinishedJob(job);
            } else {
              jobRuntime = this.timeService.getCurrentRuntimeOfJob(job);
            }
            job.setRuntime(jobRuntime);
            tmpJobs.push(job);
          }
          this.jobs = tmpJobs;
          this.prepareProgram(this.program);
        },
        error => console.error('Error while fetching jobs: ' + error)
      );
  }

  private calculateProgressInPercent(): void {
    let tasksCounter = 0;
    let finishedTasksCounter = 0;

    for (const job of this.jobs) {
      tasksCounter += job.getNrOfTasks();
      finishedTasksCounter += job.getNrOfFinishedTasks();
    }
    if (tasksCounter !== 0) {
      this.percent = (finishedTasksCounter / tasksCounter) * 100;
    } else {
      this.percent = 0;
    }
  }

  deleteProgram(): void {
    this.programService.deleteProgram(this.program.id).subscribe(
      state => {
        if (state === 'DONE') {
          this.router.navigateByUrl('/programs');
        } else if (status === 'FAILED') {
          alert('Can not delete program.');
        }
      }, error => {
        console.log('Error while delete program: ' + error);
      });
  }

  abortProgram(): void {
    this.programService.abortProgram(this.program.id).subscribe(
      state => {
        if (state === 'FAILED') {
          alert('Could not abort Program.');
        }
        this.fetchProgram(this.program.id);
      },
      error => {
        console.log('Error while abort Program: ' + error);
      });
  }

  updateProgramName(): void {
    this.program.name = this.newProgramName;
    this.programService.updateProgram(this.program).subscribe(
      state => {
        if (state === 'FAILED') {
          alert('Could not update Program name.');
        } else {
          this.fetchProgram(this.program.id);
        }
      },
      error => {
        console.log('Error while update Program name: ' + error);
      });
  }

  cancelUpdatingProgramName(): void {
    this.newProgramName = this.program.name;
  }

  updateProgramDescription(): void {
    this.program.description = this.newProgramDescription;
    this.programService.updateProgram(this.program).subscribe(
      state => {
        if (state === 'FAILED') {
          alert('Could not update Program description.');
        } else {
          this.fetchProgram(this.program.id);
        }
      },
      error => {
        console.log('Error while update Program description: ' + error);
      });
  }

  cancelUpdatingProgramDescription(): void {
    this.newProgramDescription = this.program.description;
  }

  openDetailsView(job: Job) {
    this.jobService.select(job);
    this.router.navigate(['./jobs/' + job.id], { relativeTo: this.route});
  }
}
