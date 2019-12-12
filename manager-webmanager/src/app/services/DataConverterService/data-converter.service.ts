import {Injectable} from '@angular/core';
import {AppConfig} from '../../config/app-config';
import {BaseService} from '../base.service';
import {HttpClient} from '@angular/common/http';
import {catchError, tap} from 'rxjs/operators';
import {Observable} from 'rxjs/internal/Observable';

@Injectable()
export class DataConverterService extends BaseService {
  private cache: Map<string, string> = new Map<string, string>();

  constructor(appConfig: AppConfig, httpClient: HttpClient) {
    super(appConfig, httpClient);
  }

  getNameOfDataType(dataTypeId: string): Observable<string> {
    if (this.cache.has(dataTypeId)) {
      return Observable.create(function (observable) {
        observable.next(this.cache[dataTypeId]);
      });
    }
    const url = this.appConfig.serviceConfig.getUrlForFetchingNameOfDataTypeWithId(dataTypeId);
    return this.httpClient.get(url, {responseType: 'text'}).pipe(
      tap(name => this.cache[dataTypeId] = name),
      catchError(this.handleError<string>('getNameOfDataType(dataTypeId)'))
    );
  }
}
