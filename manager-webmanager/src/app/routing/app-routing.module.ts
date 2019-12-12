import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from "../components/login/login.component";
import { ProgramOverviewComponent } from "../components/program-overview/program-overview.component";
import { ProgramDetailsComponent } from "../components/program-details/program-details.component";
import { JobDetailsComponent } from "../components/job-details/job-details.component";
import { TaskDetailsComponent } from "../components/task-details/task-details.component";
import { ClusterOverviewComponent } from "../components/cluster-overview/cluster-overview.component";
import { ClusterDetailsComponent } from "../components/cluster-details/cluster-details.component";
import { LibraryOverviewComponent } from "../components/library-overview/library-overview.component";
import { NodeDetailsComponent } from "../components/node-details/node-details.component";
import { LibraryRoutineAddComponent } from "../components/library-routine-add/library-routine-add.component";
import { LibraryDatatypeDetailComponent } from "../components/library-datatype-detail/library-datatype-detail.component";
import { LibraryDatatypeAddComponent } from "../components/library-datatype-add/library-datatype-add.component";
import { ResourcesComponent } from "../components/resources/resources.component";
import { LibraryRoutineDetailComponent } from "../components/library-routine-detail/library-routine-detail.component";
import { AuthGuard } from "./guards/AuthGuard/auth.guard";
import { ManagerComponent } from "../components/manager/manager.component";
import {LibraryFeatureAddComponent} from "../components/library-feature-add/library-feature-add.component";

const routes: Routes = [

  { path: '', redirectTo: 'manager', pathMatch: 'full'},
  { path: 'login', component: LoginComponent },
  { path: 'manager', component: ManagerComponent, canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'programs', pathMatch: 'full'},
      { path: 'programs', component: ProgramOverviewComponent },
      { path: 'programs/:pId', component: ProgramDetailsComponent },

      { path: 'programs/:pId/jobs/:jId', component: JobDetailsComponent },
      { path: 'programs/:pId/jobs/:jId/tasks/:tId', component: TaskDetailsComponent },

      { path: 'clusters', component: ClusterOverviewComponent },
      { path: 'clusters/:cId', component: ClusterDetailsComponent },
      { path: 'clusters/:cId/nodes/:nId', component: NodeDetailsComponent },

      { path: 'library', redirectTo: 'library/routines', pathMatch: 'full' },
      { path: 'library/routines', component: LibraryOverviewComponent },
      { path: 'library/routines/routine-add', component: LibraryRoutineAddComponent },
      { path: 'library/routines/routine-detail/:rId', component: LibraryRoutineDetailComponent },
      { path: 'library/datatypes', component: LibraryOverviewComponent },
      { path: 'library/datatypes/datatype-detail/:dId', component: LibraryDatatypeDetailComponent },
      { path: 'library/datatypes/datatype-add', component: LibraryDatatypeAddComponent },
      { path: 'library/tags', component: LibraryOverviewComponent },
      { path: 'library/features', component: LibraryOverviewComponent },
      { path: 'library/features/feature-add', component: LibraryFeatureAddComponent },

      { path: 'resources', component: ResourcesComponent}
    ]
  }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule { }
