import {FormalParameter} from './formal-parameter';
import {RoutineBinary} from './routine-binary';
import {Feature} from "./feature";

export enum RoutineType {
  OBJECTIVE = 0,
  MAP = 1,
  STORE = 2,
  REDUCE = 3,
  CLIENT = 4
}

export enum Language {
  JAVA = 0,
  PYTHON = 1,
  MATLAB = 2,
  CPP = 3,
  CSHARP = 4
}

export class Routine {
  id: string;
  privateRoutine: boolean;
  name: string;
  description: string;
  revision: number;
  type: RoutineType;
  inParameters: FormalParameter[] = [];
  outParameter: FormalParameter;
  routineBinaries: RoutineBinary[] = [];
  arguments: string[];
  requiredFeatures: Feature[] = [];

  constructor(jsonData?: any) {
    this.id = jsonData && jsonData.id || '';
    this.privateRoutine = jsonData && jsonData.privateRoutine || false;
    this.name = jsonData && jsonData.name || '';
    this.description = jsonData && jsonData.description || '';
    this.revision = jsonData && jsonData.revision || 1;
    if (jsonData && jsonData.type) {
      this.type = this.getRoutineType(jsonData.type);
    } else {
      this.type = RoutineType.OBJECTIVE;
    }
    if (jsonData && jsonData.inParameters) {
      for (const jsonInParam of jsonData.inParameters) {
        this.inParameters.push(new FormalParameter(jsonInParam));
      }
    }
    if (jsonData && jsonData.outParameter) {
      this.outParameter = new FormalParameter(jsonData.outParameter);
    }
    this.arguments = jsonData && jsonData.arguments || [];
    if (jsonData && jsonData.requiredFeatures) {
      for (const jsonRequiredFeature of jsonData.requiredFeatures) {
        this.requiredFeatures.push(new Feature(jsonRequiredFeature));
      }
    }
  }

  private getRoutineType(key: string): RoutineType {
    return RoutineType[key];
  }

  private getLanguage(key: string): Language {
    return Language[key];
  }
}
