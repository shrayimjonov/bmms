import {Routes} from '@angular/router';
import {CanActivateUserRoute} from './core/auth/can-activate-user-route';
import {ERROR_ROUTES} from './core/error/error.route';

export const APP_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule),
    canActivate: [CanActivateUserRoute],
  },
  {
    path: 'authorization',
    loadChildren: () => import('./authorization/authorization.module').then(m => m.AuthorizationModule),
  },
  {
    path: 'checkout',
    loadChildren: () => import('./checkout/checkout.module').then(m => m.CheckoutModule),
  },
  ...ERROR_ROUTES,
];
