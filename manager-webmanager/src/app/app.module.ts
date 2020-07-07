/** Modules **/
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './routing/app-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
//import { LoadingModule } from 'ngx-loading';
import { NgCircleProgressModule } from 'ng-circle-progress';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { HighlightModule } from 'ngx-highlightjs';
import { ContenteditableModule} from "@ng-stack/contenteditable";

/** Components */
import { AppComponent } from './app.component';
import { ProgramOverviewComponent } from './components/program-overview/program-overview.component';
import { ProgramDetailsComponent } from './components/program-details/program-details.component';
import { JobDetailsComponent } from './components/job-details/job-details.component';
import { TaskDetailsComponent } from './components/task-details/task-details.component';
import { ClusterOverviewComponent } from './components/cluster-overview/cluster-overview.component';
import { ClusterDetailsComponent } from './components/cluster-details/cluster-details.component';
import { NodeDetailsComponent } from './components/node-details/node-details.component';
import { LibraryOverviewComponent } from './components/library-overview/library-overview.component';
import { LibraryRoutineAddComponent } from './components/library-routine-add/library-routine-add.component';
import { LibraryDatatypeDetailComponent } from './components/library-datatype-detail/library-datatype-detail.component';
import { LibraryRoutineDetailComponent } from './components/library-routine-detail/library-routine-detail.component';
import { LibraryDatatypeAddComponent } from './components/library-datatype-add/library-datatype-add.component';
import { ResourcesComponent } from './components/resources/resources.component';
import { NavigationBreadcrumbComponent } from './components/navigation-breadcrumb/navigation-breadcrumb.component';
import { LoginComponent } from './components/login/login.component';
import { ManagerComponent } from './components/manager/manager.component';

/** Services **/
import { AppConfig } from './config/app-config';
import { ProgramService } from './services/ProgramService/program.service';
import { JobService } from './services/JobService/job.service';
import { TaskService } from './services/TaskService/task.service';
import { ClusterService } from './services/ClusterService/cluster.service';
import { NodeService } from './services/NodeService/node.service';
import { TimeService } from './services/TimeService/time.service';
import { DataConverterService } from './services/DataConverterService/data-converter.service';
import { LibraryService } from './services/LibraryService/library.service';
import { DatePipe } from '@angular/common';
import { LoginService } from './services/LoginService/login.service';

/** Font Awesome Modules **/
//import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
//import { library } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeModule, FaIconLibrary } from '@fortawesome/angular-fontawesome';

/** Font Awesome Icons **/
import {
  fas,
  faQuestionCircle,
  faCoffee,
  faClock,
  faSync,
  faCheck,
  faExclamation,
  faTimes,
  faEdit,
  faServer,
  faCopy,
  faMicrochip,
  faSearch,
  faPlus,
  faUpload,
  faRocket,
  faSave,
  faDownload,
  faDatabase,
  faArchive,
  faCaretDown,
  faCaretUp,
  faSignOutAlt,
  faUserCircle,
  faPlay,
  faRandom,
  faDesktop,
  faCompress,
  faChevronRight,
  faChevronDown,
  faCompressArrowsAlt
} from '@fortawesome/free-solid-svg-icons';
import { LibraryFeatureAddComponent } from './components/library-feature-add/library-feature-add.component';
import { FeatureListComponent } from './components/feature-list/feature-list.component';
import {LoadingModule} from "ngx-loading";


@NgModule({
  declarations: [
    AppComponent,
    ProgramOverviewComponent,
    ProgramDetailsComponent,
    JobDetailsComponent,
    TaskDetailsComponent,
    ClusterOverviewComponent,
    ClusterDetailsComponent,
    NodeDetailsComponent,
    LibraryOverviewComponent,
    LibraryRoutineAddComponent,
    LibraryDatatypeDetailComponent,
    LibraryRoutineDetailComponent,
    LibraryDatatypeAddComponent,
    ResourcesComponent,
    NavigationBreadcrumbComponent,
    LoginComponent,
    ManagerComponent,
    LibraryFeatureAddComponent,
    FeatureListComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FontAwesomeModule,
    HttpClientModule,
    ContenteditableModule,
    FormsModule,
    NgSelectModule,
    /*LoadingModule.forRoot({
      backdropBackgroundColour: 'rgba(0,0,0,0.0)',
      primaryColour: '#8c8c8c',
      secondaryColour: '#8c8c8c',
      tertiaryColour: '#8c8c8c'
    }),*/
    NgCircleProgressModule.forRoot({
      radius: 100,
      maxPercent: 100,
      outerStrokeWidth: 12,
      innerStrokeWidth: 5,
      outerStrokeColor: '#78C000',
      innerStrokeColor: '#C7E596',
      animationDuration: 300
    }),
    ConfirmationPopoverModule.forRoot({
      confirmButtonType: 'danger'
    }),
    TooltipModule.forRoot(),
    HighlightModule,
    LoadingModule
  ],
  providers: [
    AppConfig,
    ProgramService,
    JobService,
    TaskService,
    ClusterService,
    NodeService,
    TimeService,
    DataConverterService,
    LibraryService,
    LoginService,
    DatePipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(library: FaIconLibrary) {
    /** Add icons to Font Awesome library **/
    library.addIconPacks(fas);
    library.addIcons(faQuestionCircle);
    library.addIcons(faCoffee);
    library.addIcons(faClock);
    library.addIcons(faSync);
    library.addIcons(faCheck);
    library.addIcons(faExclamation);
    library.addIcons(faTimes);
    library.addIcons(faEdit);
    library.addIcons(faServer);
    library.addIcons(faCopy);
    library.addIcons(faMicrochip);
    library.addIcons(faSearch);
    library.addIcons(faPlus);
    library.addIcons(faUpload);
    library.addIcons(faRocket);
    library.addIcons(faSave);
    library.addIcons(faDownload);
    library.addIcons(faDatabase);
    library.addIcons(faArchive);
    library.addIcons(faCaretDown);
    library.addIcons(faCaretUp);
    library.addIcons(faSignOutAlt);
    library.addIcons(faUserCircle);
    library.addIcons(faPlay);
    library.addIcons(faRandom);
    library.addIcons(faDesktop);
    library.addIcons(faCompress);
    library.addIcons(faChevronRight);
    library.addIcons(faChevronDown);
    library.addIcons(faCompressArrowsAlt);
  }
}
