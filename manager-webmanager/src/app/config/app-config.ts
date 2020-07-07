import {Injectable } from '@angular/core';
import {ServiceConfig} from './service-config';

@Injectable()
export class AppConfig {

  readonly serviceConfig: ServiceConfig;
  readonly updateInterval: number = 5000;

  constructor() {
    // ToDo: Read from config.json
    this.serviceConfig = new ServiceConfig('http', 'manager', '40060', '/api');
  }
}
