import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserGroupAuthority } from '../user-group/user-group.model';
import { ApplicationConfigService } from '../../../../core/config/application-config.service';
import { createRequestOption } from '../../../../core/util/request-util';

@Injectable({ providedIn: 'root' })
export class AuthorityService {
  resourceUrl = this.applicationConfigService.getEndpointFor('api/keycloak/roles');

  constructor(private http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  create(authority: UserGroupAuthority): Observable<UserGroupAuthority> {
    return this.http.post<UserGroupAuthority>(this.resourceUrl + '/create', authority, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<UserGroupAuthority[]>> {
    return this.http.get<UserGroupAuthority[]>(this.resourceUrl, {
      observe: 'response',
      params: createRequestOption(req),
    });
  }

  delete(name: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${name}`, { observe: 'response' });
  }

  byGroup(id: string | number): Observable<HttpResponse<UserGroupAuthority[]>> {
    return this.http.get<UserGroupAuthority[]>(`${this.resourceUrl}/by-group/${id}`, { observe: 'response' });
  }
}
