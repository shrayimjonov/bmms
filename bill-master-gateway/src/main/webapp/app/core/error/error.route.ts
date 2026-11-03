import { Routes } from '@angular/router';
import { ErrorComponent } from './error.component';

export const ERROR_ROUTES: Routes = [
  {
    path: 'error',
    component: ErrorComponent,
    data: {
      pageTitle: 'error.title',
    },
  },
  {
    path: '403',
    component: ErrorComponent,
    data: {
      pageTitle: 'error.title',
      status: '403',
    },
  },
  {
    path: '404',
    component: ErrorComponent,
    data: {
      pageTitle: 'error.title',
      status: '404',
    },
  },
  {
    path: '500',
    component: ErrorComponent,
    data: {
      pageTitle: 'error.title',
      status: '500',
    },
  },
  {
    path: '**',
    redirectTo: '/404',
  },
];
