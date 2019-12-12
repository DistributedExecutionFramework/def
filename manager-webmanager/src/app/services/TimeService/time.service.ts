import { Injectable } from '@angular/core';
import {DatePipe} from '@angular/common';
import {Program} from '../../entities/program';
import {Job} from '../../entities/job';
import {Task} from '../../entities/task';

@Injectable()
export class TimeService {

  constructor(private datePipe: DatePipe) { }

  getDateAndTimeFromMilliSeconds(milliSeconds: number): string {
    return this.datePipe.transform(milliSeconds, 'dd.MM.yyyy HH:mm:ss');
  }

  private getHoursOfMilliSeconds(milliSeconds: number): number {
    return Math.floor(milliSeconds / 1000 / 60 / 60);
  }

  private getMinutesOfMilliSeconds(milliSeconds: number): number {
    return Math.floor((milliSeconds / 1000 / 60) % 60);
  }

  private getSecondsOfMilliSeconds(milliSeconds: number): number {
    return Math.floor((milliSeconds / 1000) % 60);
  }

  formatRuntime(runtime: number) {
    const hours = this.getHoursOfMilliSeconds(runtime);
    const minutes = this.getMinutesOfMilliSeconds(runtime);
    const seconds = this.getSecondsOfMilliSeconds(runtime);
    const hoursString = (hours < 10) ? ('0' + hours) : hours;
    const minutesString = (minutes < 10) ? ('0' + minutes) : minutes;
    const secondsString = (seconds < 10) ? ('0' + seconds) : seconds;
    return hoursString + ':' + minutesString + ':' + secondsString;
  }

  getCurrentRuntimeOfProgram(program: Program): number {
    const end = new Date();
    const start = new Date(program.createTime);
    return end.getTime() - start.getTime();
  }

  getRuntimeOfFinishedProgram(program: Program) {
    return program.finishTime - program.createTime;
  }

  getCurrentRuntimeOfJob(job: Job): number {
    const end = new Date();
    const start = new Date(job.startTime);
    return end.getTime() - start.getTime();
  }

  getRuntimeOfFinishedJob(job: Job) {
    return job.finishTime - job.startTime;
  }

  getCurrentRuntimeOfTask(task: Task): number {
    const end = new Date();
    const start = new Date(task.startTime);
    return end.getTime() - start.getTime();
  }

  getRuntimeOfFinishedTask(task: Task) {
    return task.finishTime - task.startTime;
  }

}
