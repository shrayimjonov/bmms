import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {SERVER_API_URL} from 'app/app.constants';

@Injectable()
export class PasswordService {
  constructor(private readonly http: HttpClient) {
  }

  save(password: string, userId: string): Observable<{}> {
    return this.http.post(SERVER_API_URL + '/api/keycloak/users/reset-password', {
      password,
      userId
    });
  }
}
