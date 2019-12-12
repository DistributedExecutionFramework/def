import {Component, OnInit} from '@angular/core';
import {FormalParameter} from '../../entities/formal-parameter';
import {DataType} from '../../entities/datatype';
import {v4 as uuid} from 'uuid';
import {LibraryService} from '../../services/LibraryService/library.service';
import {Observable} from 'rxjs/internal/Observable';
import {RoutineBinary} from '../../entities/routine-binary';
import {ParallelHasher} from 'ts-md5/dist/parallel_hasher';
import {Language, Routine, RoutineType} from '../../entities/routine';
import {filter, finalize} from 'rxjs/operators';
import {ActivatedRoute, Router} from '@angular/router';
import {NavigationElement} from '../../routing/navigation-element';
import {Feature} from "../../entities/feature";

@Component({
  selector: 'app-library-routine-add',
  templateUrl: './library-routine-add.component.html',
  styleUrls: ['./library-routine-add.component.css']
})
export class LibraryRoutineAddComponent implements OnInit {

  navigationPath: NavigationElement[] = [];
  activeNavigationElement: NavigationElement;
  private readonly hasher: ParallelHasher = new ParallelHasher('./assets/md5_worker.js');
  name: string;
  private: boolean;
  description: string;
  languageFeature: Feature;
  type: RoutineType;
  arguments: string;
  inParameters: FormalParameter[] = [];
  outParameter: FormalParameter;
  dataTypes: Observable<DataType[]>;
  routineBinaries: RoutineBinary[] = [];
  submitting = false;
  allFeatures: Observable<Feature[]>;
  requiredFeatures: Feature[] = [];
  languageFeatures: Feature[] = [];
  nonLanguageFeatures: Feature[] = [];

  constructor(
    private libraryService: LibraryService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {

    // Update navigation path
    this.setNavigationPath();

    this.dataTypes = this.libraryService.getAllDataTypes();
    this.allFeatures = this.libraryService.getAllFeatures();
    this.type = RoutineType.OBJECTIVE;
    this.outParameter = new FormalParameter();
    this.outParameter.id = uuid();
    this.outParameter.name = 'result';
    this.dataTypes.subscribe(dataTypes => this.outParameter.dataType = dataTypes[0]);
    this.allFeatures.subscribe(features => {
      for (let feature of features) {
        if (feature.group === 'language') {
          this.languageFeatures.push(feature);
          if (!this.languageFeature) {
            this.languageFeature = feature;
            this.languageChange();
          }
        } else {
          this.nonLanguageFeatures.push(feature);
        }
      }
    });
  }

  setNavigationPath(): void {
    this.navigationPath.push(new NavigationElement('Library Overview', '../../', ''));
    this.navigationPath.push(new NavigationElement('Routines', '../', ''));
    this.activeNavigationElement = new NavigationElement(
      'Add Routine',
      './',
      ''
    );
  }


  addInParameter(): void {
    const f = new FormalParameter();
    f.id = uuid();
    this.dataTypes.subscribe(value => f.dataType = value[0]);
    this.inParameters.push(f);
  }

  removeInParameter(param: FormalParameter): void {
    const i = this.inParameters.indexOf(param);
    this.inParameters.splice(i, 1);
  }

  addRoutineBinary(): void {
    const rb = new RoutineBinary();
    rb.id = uuid();
    if (this.routineBinaries.length === 0) {
      rb.primary = true;
    } else {
      rb.primary = false;
    }
    this.routineBinaries.push(rb);
  }

  removeRoutineBinary(routineBinary: RoutineBinary): void {
    const i = this.routineBinaries.indexOf(routineBinary);
    this.routineBinaries.splice(i, 1);
  }

  fileSelect(routineBinary: RoutineBinary, event: any): void {
    const file: File = event.target.files[0];
    if (file !== undefined && file != null) {
      routineBinary.sizeInBytes = file.size;

      const fileReader = new FileReader();
      fileReader.onloadend = e => {
        routineBinary.data = fileReader.result;
        const blob = new Blob([fileReader.result]);
        this.hasher.hash(blob).then(
          md5 => routineBinary.md5 = md5
        );
      };
      fileReader.readAsArrayBuffer(file);
    }
  }

  onSubmit(): void {
    const routine = new Routine();
    routine.id = uuid();
    routine.name = this.name;
    routine.description = this.description;
    routine.revision = 1;
    //routine.language = this.language;
    routine.type = this.type;
    routine.privateRoutine = this.private;
    if (this.arguments.length > 0) {
      routine.arguments = this.arguments.split('\n');
    }
    routine.inParameters = this.inParameters;
    routine.outParameter = this.outParameter;
    routine.requiredFeatures = [];
    // First add language feature
    this.languageFeature.extensions = [];
    routine.requiredFeatures.push(this.languageFeature);
    // Add all 'base' features and remove all extensions
    for (let f of this.requiredFeatures) {
      if (!f.baseId || f.baseId == '') {
        f.baseId = undefined;
        f.extensions = [];
        routine.requiredFeatures.push(f);
      }
    }
    // Add all extensions to the correct base feature
    for (let e of this.requiredFeatures) {
      if (e.baseId) {
        e.extensions = [];
        for (let f of routine.requiredFeatures) {
          if (f.id === e.baseId) {
            f.extensions.push(e);
            break;
          }
        }
      }
    }

    this.submitting = true;
    this.libraryService.createRoutine(routine)
      .pipe(finalize(() => {
        this.submitting = false;
        this.router.navigate(['../'], {relativeTo: this.route, queryParams: {routineFilter: this.name}});
      }))
      .subscribe(
        rId => this.uploadBinaries(rId)
      );
  }

  languageChange(): void {
    let lang = this.languageFeature.name.toLowerCase();
    if (lang.includes('java') || lang.includes('c#')) {
      this.arguments = 'full.class.Name';
    } else if (lang.includes('matlab')) {
      this.arguments = 'functionName';
    } else {
      this.arguments = '';
    }
    this.requiredFeatures = [];
  }

  private uploadBinaries(rId: string) {
    this.routineBinaries.forEach(async binary => {
      await this.libraryService.uploadBinary(rId, binary).toPromise();
    });
  }

  getLanguages(): Language[] {
    return this.libraryService.getLanguages();
  }

  getRoutineTypes(): RoutineType[] {
    return this.libraryService.getRoutineTypes();
  }

  getLanguageName(language: Language): string {
    return this.libraryService.getLanguageName(language);
  }

  getRoutineTypeName(routineType: RoutineType): string {
    return this.libraryService.getRoutineTypeName(routineType);
  }

  isRequiredFeatureActive(feature: Feature): boolean {
    for (let f of this.requiredFeatures) {
      if (f.id === feature.id) {
        return true;
      }
    }
    return false;
  }

  setRequiredFeature(feature: Feature): void {
    let i = 0;
    for (let f of this.requiredFeatures) {
      if (f.id === feature.id) {
        this.requiredFeatures.splice(i, 1);
        return;
      }
      i++;
    }
    this.requiredFeatures.push(feature);
  }
}
