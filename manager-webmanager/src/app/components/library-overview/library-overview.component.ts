import {Component, OnDestroy, OnInit} from '@angular/core';
import {Language, Routine, RoutineType} from '../../entities/routine';
import {LibraryService} from '../../services/LibraryService/library.service';
import {finalize} from 'rxjs/operators';
import {DataType} from '../../entities/datatype';
import {ActivatedRoute, Router} from '@angular/router';
import {NavigationElement} from "../../routing/navigation-element";
import {Feature} from "../../entities/feature";
import {LoginService} from "../../services/LoginService/login.service";

export enum LibraryOverviewTab {
  ROUTINES,
  DATA_TYPES,
  FEATURES,
  TAGS
}

@Component({
  selector: 'app-library-overview',
  templateUrl: './library-overview.component.html',
  styleUrls: ['./library-overview.component.css']
})
export class LibraryOverviewComponent implements OnInit, OnDestroy {
  activeNavigationElement: NavigationElement;
  tabs = LibraryOverviewTab;
  activeTab: LibraryOverviewTab = LibraryOverviewTab.ROUTINES;
  routines: Routine[] = [];
  routinesFilter = '';
  routinesLoading = true;
  dataTypes: DataType[] = [];
  dataTypesFilter = '';
  dataTypesLoading = true;
  features: Feature[] = [];
  featuresFilter = '';
  featuresLoading = true;

  constructor(
    public libraryService: LibraryService,
    public loginService: LoginService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.setNavigationPath();

    this.route.url
      .subscribe(urlSegments => {
        switch (urlSegments[urlSegments.length - 1].toString()) {
          case 'routines':
            this.findRoutines();
            this.activeTab = LibraryOverviewTab.ROUTINES;
            break;
          case 'datatypes':
            this.findDataTypes();
            this.activeTab = LibraryOverviewTab.DATA_TYPES;
            break;
          case 'features':
            this.findFeatures();
            this.activeTab = LibraryOverviewTab.FEATURES;
            break;
          case 'tags':
            this.activeTab = LibraryOverviewTab.TAGS;
            break;
        }
      });

    this.route.queryParams
      .subscribe(params => {
        if (params.routineFilter !== undefined) {
          this.routinesFilter = params.routineFilter;
        }
        if (params.dataTypeFilter !== undefined) {
          this.dataTypesFilter = params.dataTypeFilter;
        }
        if (params.featureFilter !== undefined) {
          this.featuresFilter = params.featureFilter;
        }
      });
  }

  ngOnDestroy(): void {
  }

  private setNavigationPath() {
    this.activeNavigationElement = new NavigationElement(
      'Library Overview',
      './',
      ''
    );
  }

  findRoutines(): void {
    this.routinesLoading = true;
    this.libraryService.findRoutines(this.routinesFilter)
      .pipe(finalize(() => this.routinesLoading = false))
      .subscribe(
        routines => this.routines = routines,
        error => console.error('Error while search/fetch routines: ' + error)
      );
  }

  findDataTypes(): void {
    this.dataTypesLoading = true;
    this.libraryService.findDataTypes(this.dataTypesFilter)
      .pipe(finalize(() => this.dataTypesLoading = false))
      .subscribe(
        dataTypes => this.dataTypes = dataTypes,
        error => console.error('Error while search/fetch data types: ' + error)
      );
  }

  findFeatures(): void {
    this.featuresLoading = true;
    this.libraryService.findFeatures(this.featuresFilter)
      .pipe(finalize(() => this.featuresLoading = false))
      .subscribe(
        features => this.features = features,
        error => console.error('Error while search/fetch features: ' + error)
      );
  }

  openDataTypeDetails(dataType: DataType): void {
    this.libraryService.selectDataType(dataType);
    this.router.navigate(['./datatype-detail/' + dataType.id], { relativeTo: this.route});
  }

  openRoutineDetails(routine: Routine): void {
    this.libraryService.selectRoutine(routine);
    this.router.navigate(['./routine-detail/' + routine.id], { relativeTo: this.route});
  }

  getLanguageName(language: Language): string {
    return this.libraryService.getLanguageName(language);
  }

  getRoutineTypeName(routineType: RoutineType): string {
    return this.libraryService.getRoutineTypeName(routineType);
  }

  addExtension(feature: Feature) {
    let extension = new Feature();
    extension.baseId = feature.id;
    extension.id = 'new';
    feature.extensions.push(extension);
  }

  cancelAddExtension(feature: Feature) {
    let last = feature.extensions[feature.extensions.length - 1];
    if (last && last.id === 'new') {
      feature.extensions.pop();
    }
  }

  showAddExtension(feature: Feature): boolean {
    if (!this.loginService.isAdmin()) {
      return false;
    }
    let last = feature.extensions[feature.extensions.length - 1];
    if (last) {
      return last.id !== 'new';
    }
    return true;
  }

  saveNewExtension(feature: Feature) {
    let last = feature.extensions[feature.extensions.length - 1];
    if (last &&  last.id === 'new') {
      this.libraryService.addExtension(feature.id, last.name, last.version)
        .subscribe(
          extensionId => last.id = extensionId,
          error => feature.extensions.pop()
        );
    }
  }
}
