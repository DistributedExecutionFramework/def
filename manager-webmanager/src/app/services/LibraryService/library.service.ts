import {Injectable} from '@angular/core';
import {AppConfig} from '../../config/app-config';
import {Language, Routine, RoutineType} from '../../entities/routine';
import {Observable} from 'rxjs/internal/Observable';
import {BaseService} from '../base.service';
import {HttpClient} from '@angular/common/http';
import {catchError, map} from 'rxjs/operators';
import {DataType} from '../../entities/datatype';
import {RoutineBinary} from '../../entities/routine-binary';
import {saveAs} from 'file-saver/FileSaver';
import {Feature} from "../../entities/feature";

@Injectable()
export class LibraryService extends BaseService {

  private readonly languages = Language;
  private readonly routineTypes = RoutineType;
  private allDataTypes: Observable<DataType[]>;
  private selectedDataType: DataType;
  private selectedRoutine: Routine;

  constructor(appConfig: AppConfig, httpClient: HttpClient) {
    super(appConfig, httpClient);
  }

  findRoutines(pattern: string): Observable<Routine[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingRoutinesWithPattern(pattern);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => this.mapRoutines(json)),
      catchError(this.handleError<Routine[]>('findRoutines(pattern)'))
    );
  }

  getRoutineById(rId: string): Observable<Routine> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingRoutineWithId(rId);
    return this.httpClient.get<object>(url).pipe(
      map(json => this.mapRoutine(json)),
      catchError(this.handleError<Routine>('getRoutineById(rId)'))
    );
  }

  findFeatures(pattern: string): Observable<Feature[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingFeaturesWithPattern(pattern);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => LibraryService.mapFeatures(json)),
      catchError(this.handleError<Feature[]>('findFeatures(pattern)'))
    );
  }

  getAllFeatures(): Observable<Feature[]> {
    return this.findFeatures('');
  }

  findDataTypes(pattern: string): Observable<DataType[]> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingDataTypesWithPattern(pattern);
    return this.httpClient.get<object[]>(url).pipe(
      map(json => this.mapDataTypes(json)),
      catchError(this.handleError<DataType[]>('findDataTypes(pattern)'))
    );
  }

  getDataTypeById(dId: string): Observable<DataType> {
    const url = this.appConfig.serviceConfig.getUrlForFetchingDataTypeWithId(dId);
    return this.httpClient.get<object>(url).pipe(
      map(json => this.mapDataType(json)),
      catchError(this.handleError<DataType>('getDataTypeById(dId)'))
    );
  }

  getAllDataTypes(): Observable<DataType[]> {
    if (this.allDataTypes == null) {
      this.allDataTypes = this.findDataTypes('');
    }
    return this.allDataTypes;
  }

  private mapRoutines(values: object[]): Routine[] {
    const routines: Routine[] = [];
    values.forEach(value => routines.push(this.mapRoutine(value)));
    return routines;
  }

  private mapRoutine(value: object): Routine {
    return new Routine(value);
  }

  private mapDataTypes(values: object[]): DataType[] {
    const dataTypes: DataType[] = [];
    values.forEach(value => dataTypes.push(this.mapDataType(value)));
    return dataTypes;
  }

  private mapDataType(value: object): DataType {
    return new DataType(value);
  }

  public static mapFeatures(values: object[]): Feature[] {
    const features: Feature[] = [];
    values.forEach(value => features.push(LibraryService.mapFeature(value)));
    return features;
  }

  public static mapFeature(value: object): Feature {
    return new Feature(value);
  }

  createRoutine(routine: Routine): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForCreatingRoutine();
    return this.httpClient.post(url, routine, {responseType: 'text'}).pipe(
      catchError(this.handleError<string>('createRoutine(routine)'))
    );
  }

  uploadBinary(rId: string, binary: RoutineBinary): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForUploadRoutineBinary(rId, binary.md5, binary.sizeInBytes, binary.primary);
    return this.httpClient.post(url, binary.data, {responseType: 'text'}).pipe(
      catchError(this.handleError<string>('uploadBinary(rId, binary)'))
    );
  }

  selectDataType(dataType: DataType): void {
    this.selectedDataType = dataType;
  }

  getSelectedDataType(): DataType {
    return this.selectedDataType;
  }

  createDataType(name: string, schema: string): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForCreatingDataType(name);
    return this.httpClient.post(url, schema, {responseType: 'text'}).pipe(
      catchError(this.handleError<string>('createDataType(schema)'))
    );
  }

  selectRoutine(routine: Routine): void {
    this.selectedRoutine = routine;
  }

  getSelectedRoutine(): Routine {
    return this.selectedRoutine;
  }

  getLanguages(): Language[] {
    const keys = Object.keys(Language);
    return keys.slice(keys.length / 2).map(name => this.languages[name]);
  }

  getRoutineTypes(): RoutineType[] {
    const keys = Object.keys(RoutineType);
    return keys.slice(keys.length / 2).map(name => this.routineTypes[name]);
  }

  getLanguageName(language: Language): string {
    switch (language.toString()) {
      case Language.JAVA.toString():
        return 'Java';
      case Language.CPP.toString():
        return 'C++';
      case Language.CSHARP.toString():
        return 'C#';
      case Language.MATLAB.toString():
        return 'MATLAB';
      case Language.PYTHON.toString():
        return 'Python';
    }
  }

  toLanguage(feature: Feature): Language {
    if (feature.group === 'language') {
      if (feature.name.toLowerCase().includes('java')) {
        return Language.JAVA;
      }
      if (feature.name.toLowerCase().includes('python')) {
        return Language.PYTHON;
      }
      if (feature.name.toLowerCase().includes('c#')) {
        return Language.CSHARP;
      }
      if (feature.name.toLowerCase().includes('c++')) {
        return Language.CPP;
      }
      if (feature.name.toLowerCase().includes('matlab')) {
        return Language.MATLAB;
      }
    }
    return null;
  }


  getRoutineTypeName(routineType: RoutineType): string {
    switch (routineType.toString()) {
      case RoutineType.OBJECTIVE.toString():
        return 'Objective';
      case RoutineType.MAP.toString():
        return 'Map';
      case RoutineType.CLIENT.toString():
        return 'Master';
      case RoutineType.REDUCE.toString():
        return 'Reduce';
      case RoutineType.STORE.toString():
        return 'Store';
    }
  }

  generateAndDownloadDataTypes(language: string, dataTypeIds: string[]): void {
    const url = this.appConfig.serviceConfig.getUrlForGeneratingDataTypes(language);
    this.httpClient.post(url, dataTypeIds, {responseType: 'blob'})
      .pipe(catchError(this.handleError<DataType>('generateAndDownloadDataTypes(language, dIds)')))
      .subscribe(data => this.downloadFile(language, data));
  }

  private downloadFile(language: string, data: any) {
    const blob = new Blob([data], {type: 'application/zip'});
    saveAs(blob, language + '_dataTypes.zip');
  }

  addExtension(baseFeatureId: string, name: string, version: string): Observable<string> {
    const url = this.appConfig.serviceConfig.getUrlForAddExtension(baseFeatureId, name, version);
    return this.httpClient.post(url, null, {responseType: 'text'}).pipe(
      catchError(this.handleError<string>('addExtension(baseFeatureId, name, version)'))
    );
  }

  createFeature(name: string, version: string, group: string) {
    const url = this.appConfig.serviceConfig.getUrlForCreateFeature(name, version, group);
    return this.httpClient.post(url, null, {responseType: 'text'}).pipe(
      catchError(this.handleError<string>('createFeature(name, version, group)'))
    );
  }

  isObjectiveRoutine(routine: Routine): boolean {
    if (!routine) {
      return false;
    }
    return routine.type == RoutineType.OBJECTIVE;
  }

  isMapRoutine(routine: Routine): boolean {
    if (!routine) {
      return false;
    }
    return routine.type == RoutineType.MAP;
  }

  isStoreRoutine(routine: Routine): boolean {
    if (!routine) {
      return false;
    }
    return routine.type == RoutineType.STORE;
  }

  isReduceRoutine(routine: Routine): boolean {
    if (!routine) {
      return false;
    }
    return routine.type == RoutineType.REDUCE;
  }

  isClientRoutine(routine: Routine): boolean {
    if (!routine) {
      return false;
    }
    return routine.type == RoutineType.CLIENT;
  }

  removeRoutine(routineId: string): Observable<Object> {
    const url = this.appConfig.serviceConfig.getUrlForRemoveRoutine(routineId);
    return this.httpClient.delete(url).pipe(
      catchError(this.handleError<Object>('removeRoutine(routineId)'))
    );
  }
}
