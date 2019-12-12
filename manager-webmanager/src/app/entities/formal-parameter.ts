import {DataType} from './datatype';

export class FormalParameter {
  id: string;
  name: string;
  description: string;
  dataType: DataType = new DataType();

  constructor(jsonData?: any) {
    this.id = jsonData && jsonData.id || '';
    this.name = jsonData && jsonData.name || '';
    this.description = jsonData && jsonData.description || '';
    if (jsonData && jsonData.dataType) {
      this.dataType = new DataType(jsonData.dataType);
    }
  }
}

