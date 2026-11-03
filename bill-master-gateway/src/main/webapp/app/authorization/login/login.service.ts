import { Injectable } from '@angular/core';
import { Location } from '@angular/common';

import { Logout } from './logout.model';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs/internal/Observable';
import { ApplicationConfigService } from '../../core/config/application-config.service';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class LoginService {
  constructor(
    private readonly appConfigService: ApplicationConfigService,
    private readonly location: Location,
    private readonly http: HttpClient
  ) {}

  login(): void {
    location.href = `${location.origin}${this.location.prepareExternalUrl('oauth2/authorization/oidc')}`;
  }

  logout(): Observable<void> {
    return this.http.post(this.appConfigService.getEndpointFor('api/logout'), null).pipe(
      map((logout: Logout) => {
        let logoutUrl = logout.logoutUrl;
        const redirectUri = `${location.origin}${this.location.prepareExternalUrl('/')}`;

        // if Keycloak, uri has protocol/openid-connect/token
        // if (logoutUrl.includes('/protocol')) {
        //   logoutUrl = logoutUrl + '?redirect_uri=' + redirectUri;
        // } else {
        //   // Okta
          logoutUrl = logoutUrl + '?id_token_hint=' + logout.idToken + '&post_logout_redirect_uri=' + redirectUri;
        // }

        window.location.href = logoutUrl;
      })
    );
  }
}
