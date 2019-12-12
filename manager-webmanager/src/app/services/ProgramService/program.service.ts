import {Injectable} from '@angular/core';
import {Program} from '../../entities/program';
import {Observable} from 'rxjs/internal/Observable';
import {catchError, map} from 'rxjs/operators';
import {BaseService} from '../base.service';
import {AppConfig} from '../../config/app-config';
import {HttpClient} from '@angular/common/http';
import {LoginService} from "../LoginService/login.service";

@Injectable()
export class ProgramService extends BaseService {
  private selected: Program;

  constructor(
    protected appConfig: AppConfig,
    protected httpClient: HttpClient,
    private loginService: LoginService) {
    super(appConfig, httpClient);
  }

  getAllPrograms(): Observable<Program[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingAllPrograms(this.loginService.getCurrentUserName());
    return this.httpClient.get<object[]>(url).pipe(
      map(json => this.mapPrograms(json)),
      catchError(this.handleError<Program[]>('getAllPrograms'))
    );
  }

  getProgram(pId: string): Observable<Program> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingProgramWithId(pId);
    return this.httpClient.get<object>(url).pipe(
      map(json => this.mapProgram(json)),
      catchError(this.handleError<Program>('getProgram(pId)'))
    );
  }

  deleteProgram(pId): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForDeletingProgramWithId(pId);
    return this.httpClient.delete<string>(url).pipe(
      catchError(this.handleError<string>('deleteProgram(pId)'))
    );
  }

  abortProgram(pId): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForAbortingProgramWithId(pId);
    return this.httpClient.put<string>(url, '').pipe(
      catchError(this.handleError<string>('abortProgram(pId)'))
    );
  }

  updateProgram(program: Program): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForUpdatingProgramWithId(program.id);
    return this.httpClient.put<string>(url, program).pipe(
      catchError(this.handleError<string>('updateProgram(program)'))
    );
  }

  private mapPrograms(values: object[]): Program[] {
    const programs: Program[] = [];
    values.forEach(value => programs.push(this.mapProgram(value)));
    return programs;
  }

  private mapProgram(value: object): Program {
    return new Program(value);
  }


  select(program: Program) {
    this.selected = program;
  }

  getSelected(): Program {
    return this.selected;
  }
}

