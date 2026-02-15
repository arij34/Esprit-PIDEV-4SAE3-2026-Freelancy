import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [

  {
    path: 'front',
    loadChildren: () =>
      import('./features/front-office/front-office.module')
      .then(m => m.FrontOfficeModule)
  },

  {
    path: '',
    redirectTo: 'front',
    pathMatch: 'full'
  },
  { 
    path: '', 
    redirectTo: 'back-office', 
    pathMatch: 'full' 
  },
  {
    path: 'back-office',
    loadChildren: () => import('./features/back-office/back-office.module').then(m => m.BackOfficeModule)
  },
  {
    path: 'front-office',
    loadChildren: () => import('./features/front-office/front-office.module').then(m => m.FrontOfficeModule)
  },
  { 
    path: '**', 
    redirectTo: 'back-office' 
  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
