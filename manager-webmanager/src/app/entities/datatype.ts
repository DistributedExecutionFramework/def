export class DataType {
  id: string;
  name: string;
  schema: string;

  constructor(jsonData?: any) {
    this.id = jsonData && jsonData.id || '';
    this.name = jsonData && jsonData.name || '';
    this.schema = jsonData && jsonData.schema || '';
  }
}
