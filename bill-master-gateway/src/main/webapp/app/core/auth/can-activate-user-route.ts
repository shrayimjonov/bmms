import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {Injectable, isDevMode} from '@angular/core';
import {AccountService} from 'app/core/auth/account.service';
import {map} from 'rxjs/operators';
import {Account} from './account.model';
import {LoginService} from '../../authorization/login/login.service';
import {StateStorageService} from "./state-storage.service";

@Injectable({providedIn: 'root'})
export class CanActivateUserRoute implements CanActivate {
  constructor(private readonly accountService: AccountService,
              private readonly loginService: LoginService,
              private readonly stateStorageService: StateStorageService,
              private readonly router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
    return this.accountService.identity().pipe(
      map((account: Account | null) => {
        if (!account) {
          this.stateStorageService.storeUrl(state.url);
          this.loginService.login();
          return false;
        }

        const authorities = route.data ? route.data['authorities'] : null;

        if (!authorities || authorities.length === 0) {
          return true;
        }

        if (this.accountService.hasAnyAuthority(authorities, route.data['notForAdmin'])) {
          return true;
        }

        if (isDevMode()) {
          console.error('User has not any of required authorities: ', authorities);
        }

        this.router.navigate(['403']).then();
        return false;
      })
    );
  }
}
