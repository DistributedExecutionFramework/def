export class Feature {
  id: string;
  baseId: string;
  name: string;
  group: string;
  version: string;
  extensions: Feature[] = [];

  constructor(jsonData?: any) {
    this.id = jsonData && jsonData.id || '';
    this.baseId = jsonData && jsonData.baseId || '';
    this.name = jsonData && jsonData.name || '';
    this.group = jsonData && jsonData.group || '';
    this.version = jsonData && jsonData.version || '';
    if (jsonData && jsonData.extensions) {
      for (const jsonExtension of jsonData.extensions) {
        this.extensions.push(new Feature(jsonExtension));
      }
    }

    // create a id if not present
    if (!this.id || this.id === '') {
      this.id = this.name + '_' + this.version;
    }
  }
}
