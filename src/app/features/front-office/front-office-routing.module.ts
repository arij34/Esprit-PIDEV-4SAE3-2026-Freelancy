import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FrontOfficeComponent } from './front-office/front-office.component';
import { ProjetClientComponent } from './front-office/components/projet-client/projet-client.component';
import { ProjetFreelancerComponent } from './front-office/components/projet-freelancer/projet-freelancer.component';
import { ProjetDetailComponent } from './front-office/components/projet-detail/projet-detail.component';
import { ProjetFreelancerDetailComponent } from './front-office/components/projet-freelancer-detail/projet-freelancer-detail.component';
import { AddProjectComponent } from './front-office/components/projet-client/add-project/add-project.component';
import { StatsComponent } from './front-office/components/stats/stats.component';
import { ProjetWorkspaceComponent } from './front-office/components/projet-workspace/projet-workspace.component';
import { WorkspaceAccessGuard } from '../../core/guards/workspace-access.guard';
const routes: Routes = [
  {
    path: '',
    component: FrontOfficeComponent
  },
  { path: 'projects',         component: ProjetClientComponent },
  { path: 'projects/add',     component: AddProjectComponent },
  { path: 'projects/:id',     component: ProjetDetailComponent },
  {
    path: 'projects/:id/workspace',
    component: ProjetWorkspaceComponent,
    canActivate: [WorkspaceAccessGuard]
  },
  { path: 'discover',         component: ProjetFreelancerComponent },
  { path: 'discover/:id',     component: ProjetFreelancerDetailComponent },
  { path: 'stats',            component: StatsComponent },
  { path: 'projet-client',    component: ProjetClientComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FrontOfficeRoutingModule { }
