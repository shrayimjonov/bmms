import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { UserGroup } from './user-group.model';
import { createRequestOption } from '../../../../core/util/request-util';
import { ApplicationConfigService } from '../../../../core/config/application-config.service';
import { IEntityConfig } from '../../../../shared/entity/entity-config.model';
import { EntityService } from '../../../../shared/entity/entity.service';
import { NglFilterField, NglFilterFieldType } from 'ngl-filter-field';
import { EntityUpdateOptions } from '../../../../shared/entity/entity-update/entity-update-options.model';
import { EntityViewOptions } from '../../../../shared/entity/entity-view/entity-view-options.model';
import { Event } from '../../../../config/event.constants';
import { AuthorityService } from '../authority/authority.service';
import { UserGroupUpdateModalComponent } from './user-group-update-modal.component';
import { finalize } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class UserGroupService implements IEntityConfig {
  resourceUrl = this.applicationConfigService.getEndpointFor('api/keycloak/groups');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private authorityService: AuthorityService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(group: UserGroup): Observable<UserGroup> {
    return this.http.post<UserGroup>(this.resourceUrl, group, { observe: 'body' });
  }

  update(group: UserGroup): Observable<UserGroup> {
    return this.http.put<UserGroup>(`${this.resourceUrl}/${group.id}`, group, { observe: 'body' });
  }

  find(id: number): Observable<UserGroup> {
    return this.http.get<UserGroup>(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<UserGroup[]>> {
    const options = createRequestOption(req);
    return this.http.get<UserGroup[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  byUser(id: number | string): Observable<HttpResponse<UserGroup[]>> {
    return this.http.get<UserGroup[]>(`${this.resourceUrl}/by-user/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [{ name: 'search', type: NglFilterFieldType.TEXT, translation: 'userGroup.name' }];
  }

  openUpdate(userGroup?: UserGroup, duplicate?: boolean): void {
    const options: EntityUpdateOptions<UserGroupUpdateModalComponent> = {
      component: UserGroupUpdateModalComponent,
      componentInstances: {
        data: userGroup || {},
        duplicate: duplicate,
      },
    };

    this.entityService.update(options);
  }

  private createViewOptions(userGroup: UserGroup): EntityViewOptions[] {
    const div = document.createElement('div');

    userGroup.authorities.forEach(authority => {
      const span: HTMLSpanElement = document.createElement('span');
      span.className = 'badge badge-info ml-2';
      span.innerText = authority;

      div.appendChild(span);
    });

    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: userGroup.id },
      { title: 'userGroup.name', value: userGroup.name },
      { title: 'userGroup.details', value: userGroup.description },
      { title: 'userGroup.authorities', value: div },
    ];

    return options;
  }

  openView(userGroup: UserGroup): void {
    this.authorityService
      .byGroup(userGroup.id)
      .pipe(finalize(() => this.entityService.view(this.createViewOptions(userGroup), userGroup.id)))
      .subscribe(res => {
        userGroup.authorities = res.body.map(it => it.name);
      });
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.USER_GROUP_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsUserGroup.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'UserGroup',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsUserGroup',
    };

    this.entityService.history(options);
  }
}
