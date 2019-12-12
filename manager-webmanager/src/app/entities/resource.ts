export class Resource {
  id: string;
  resourceType: string;
  dataTypeId: string;
  dataTypeName: string;
  binary: any;
  url: string;
  key: string;
  decodedData: string;
  displayName: string;

  constructor(jsonData: any) {
    this.id = jsonData.id;
    this.resourceType = jsonData.type;
    this.dataTypeId = jsonData.dataTypeId;
    this.binary = jsonData.data;
    jsonData.url ? this.url = jsonData.url : this.url = '';
    jsonData.key ? this.key = jsonData.key : this.key = '';
  }
}


