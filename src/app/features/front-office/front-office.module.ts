import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FrontOfficeRoutingModule } from './front-office-routing.module';
import { FrontOfficeComponent } from './front-office/front-office.component';
import { SharedModule } from '../../shared/shared.module';  
import { ProjetClientComponent } from './front-office/components/projet-client/projet-client.component';
import { AddProjectComponent } from './front-office/components/projet-client/add-project/add-project.component';
import { ProjetFreelancerComponent } from './front-office/components/projet-freelancer/projet-freelancer.component';
import { ProjetDetailComponent } from './front-office/components/projet-detail/projet-detail.component';
import { ProjetFreelancerDetailComponent } from './front-office/components/projet-freelancer-detail/projet-freelancer-detail.component';
import { StatsComponent } from './front-office/components/stats/stats.component';
import { ProposalFilterPipe } from './front-office/components/projet-client/proposal-filter.pipe';
import { ProjetWorkspaceComponent } from './front-office/components/projet-workspace/projet-workspace.component';

@NgModule({
  declarations: [
    FrontOfficeComponent,
     ProjetClientComponent,
    AddProjectComponent,
    ProjetFreelancerComponent,
    ProjetDetailComponent ,
    ProjetFreelancerDetailComponent,
    StatsComponent,
    ProposalFilterPipe,
    ProjetWorkspaceComponent
  ],
  imports: [
    CommonModule,
    FrontOfficeRoutingModule,
    SharedModule,
    FormsModule, 
  ]
})
export class FrontOfficeModule { }
