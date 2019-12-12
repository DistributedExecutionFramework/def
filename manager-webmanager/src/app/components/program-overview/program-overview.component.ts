import {Component, OnDestroy, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {Program} from '../../entities/program';
import {ActivatedRoute, Router} from '@angular/router';
import {JobService} from '../../services/JobService/job.service';
import {TimeService} from '../../services/TimeService/time.service';
import {ProgramService} from '../../services/ProgramService/program.service';
import {interval} from 'rxjs/internal/observable/interval';
import {AppConfig} from '../../config/app-config';
import {Subscription} from 'rxjs/internal/Subscription';
import {NavigationElement} from "../../routing/navigation-element";

@Component({
  selector: 'app-program-overview',
  templateUrl: './program-overview.component.html',
  styleUrls: ['./program-overview.component.css']
})
export class ProgramOverviewComponent implements OnInit, OnDestroy {
  openPrograms: Program[] = [];
  openProgramsLoading = true;
  finishedPrograms: Program[] = [];
  finishedProgramsLoading = true;

  activeNavigationElement: NavigationElement;
  private subscription: Subscription;

  constructor(
    private programService: ProgramService,
    private jobService: JobService,
    public timeService: TimeService,
    private router: Router,
    private route: ActivatedRoute,
    private location: Location,
    private appConfig: AppConfig
  ) { }

  ngOnInit() {
    this.setNavigationPath();

    // update/fetch programs manually
    this.updatePrograms();
    // update/fetch programs automatically
    this.subscription = interval(this.appConfig.updateInterval).pipe().subscribe(() => {
      if (this.location.path() === '/manager/programs') {
        this.updatePrograms();
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private setNavigationPath() {
    this.activeNavigationElement = new NavigationElement(
      'Program Overview',
      './',
      '');
  }

  private updatePrograms(): void {
    this.programService.getAllPrograms().subscribe(
      programs => this.preparePrograms(programs)
    );
  }

  private preparePrograms(programs: Program[]) {
    if (programs == null) {
      return;
    }

    const tmpFinishedPrograms = [];
    const tmpOpenPrograms = [];

    // Get all programs and split it to open and finished
    for (const program of programs) {
      if (program.state === 'SCHEDULED' || program.state === 'RUN') {
        let nrOfFinishedJobs = 0;
        const filteredPrograms = this.openPrograms.filter(p => p.id === program.id);
        if (filteredPrograms.length === 1) { // found one old program
          nrOfFinishedJobs = filteredPrograms[0].nrOfFinishedJobs;
        }
        program.nrOfFinishedJobs = nrOfFinishedJobs;
        this.jobService.getNrOfFinishedJobsOfProgram(program.id).subscribe(
          numberOfFinishedJobs => program.nrOfFinishedJobs = numberOfFinishedJobs
        );
        tmpOpenPrograms.push(program);
      } else {
        tmpFinishedPrograms.push(program);
      }
    }

    tmpOpenPrograms.sort((a, b) => {
      if (a.createTime < b.createTime) {
        return -1;
      } else if (a.createTime > b.createTime) {
        return 1;
      } else {
        return 0;
      }
    });
    tmpOpenPrograms.forEach(item => {

      item.runtime = this.timeService.getCurrentRuntimeOfProgram(item);
    });
    this.openPrograms = tmpOpenPrograms;
    this.openProgramsLoading = false;

    tmpFinishedPrograms.sort((a, b) => {
      if (a.finishTime < b.finishTime) {
        return 1;
      } else if (a.createTime > b.createTime) {
        return -1;
      } else {
        return 0;
      }
    });
    tmpFinishedPrograms.forEach(item => {
      item.runtime = this.timeService.getRuntimeOfFinishedProgram(item);
    });
    this.finishedPrograms = tmpFinishedPrograms;
    this.finishedProgramsLoading = false;
  }

  openDetailsView(program: Program) {
    this.programService.select(program);
    this.router.navigate(['./' + program.id], { relativeTo: this.route});
  }
}
