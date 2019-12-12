/** Modules **/
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './routing/app-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { LoadingModule } from 'ngx-loading';
import { NgCircleProgressModule } from 'ng-circle-progress';
import { ConfirmationPopoverModule } from 'angular-confirmation-popover';
import { TooltipModule } from 'ngx-bootstrap';
import { HighlightModule } from 'ngx-highlightjs';
import { ContenteditableModule } from 'ng-contenteditable';

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
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';

/** Font Awesome Icons **/
import {
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


/** Add icons to Font Awesome library **/
library.add(faQuestionCircle);
library.add(faCoffee);
library.add(faClock);
library.add(faSync);
library.add(faCheck);
library.add(faExclamation);
library.add(faTimes);
library.add(faEdit);
library.add(faServer);
library.add(faCopy);
library.add(faMicrochip);
library.add(faSearch);
library.add(faPlus);
library.add(faUpload);
library.add(faRocket);
library.add(faSave);
library.add(faDownload);
library.add(faDatabase);
library.add(faArchive);
library.add(faCaretDown);
library.add(faCaretUp);
library.add(faSignOutAlt);
library.add(faUserCircle);
library.add(faPlay);
library.add(faRandom);
library.add(faDesktop);
library.add(faCompress);
library.add(faChevronRight);
library.add(faChevronDown);
library.add(faCompressArrowsAlt);

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
    LoadingModule.forRoot({
      backdropBackgroundColour: 'rgba(0,0,0,0.0)',
      primaryColour: '#8c8c8c',
      secondaryColour: '#8c8c8c',
      tertiaryColour: '#8c8c8c'
    }),
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
    HighlightModule.forRoot({theme: 'atom-one-light'})
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
export class AppModule { }
