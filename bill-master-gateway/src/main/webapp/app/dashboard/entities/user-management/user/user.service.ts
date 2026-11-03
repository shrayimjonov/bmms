import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {ApplicationConfigService} from 'app/core/config/application-config.service';
import {createRequestOption} from 'app/core/util/request-util';
import {User} from './user.model';
import {UserGroup} from '../user-group/user-group.model';
import {IEntityConfig} from '../../../../shared/entity/entity-config.model';
import {EntityService} from '../../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../../config/event.constants';
import {UserUpdateModalComponent} from './user-update-modal.component';
import {UserGroupService} from '../user-group/user-group.service';
import {finalize} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class UserService implements IEntityConfig {
  resourceUrl = this.applicationConfigService.getEndpointFor('api/keycloak/users');

  constructor(
    private http: HttpClient,
    private entityService: EntityService,
    private applicationConfigService: ApplicationConfigService,
    protected groupService: UserGroupService
  ) {}

  query(req?: any): Observable<HttpResponse<User[]>> {
    const options = createRequestOption(req);
    return this.http.get<User[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  create(user: User): Observable<User> {
    return this.http.post<User>(this.resourceUrl, user);
  }

  update(user: User): Observable<User> {
    return this.http.put<User>(this.resourceUrl, user);
  }

  find(login: string): Observable<User> {
    return this.http.get<User>(`${this.resourceUrl}/${login}`);
  }

  delete(login: string): Observable<{}> {
    return this.http.delete(`${this.resourceUrl}/${login}`);
  }

  authorities(): Observable<string[]> {
    return this.http.get<string[]>(this.resourceUrl + '/authorities');
  }

  getUserGroups(req?: any): Observable<HttpResponse<UserGroup[]>> {
    const options = createRequestOption(req);
    return this.http.get<UserGroup[]>(this.applicationConfigService.getEndpointFor('api/keycloak/groups'), {
      params: options,
      observe: 'response',
    });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'username', type: NglFilterFieldType.TEXT, translation: 'userManagement.login' },
      { name: 'email', type: NglFilterFieldType.TEXT, translation: 'userManagement.email' },
      { name: 'firstName', type: NglFilterFieldType.TEXT, translation: 'userManagement.firstName' },
      { name: 'lastName', type: NglFilterFieldType.TEXT, translation: 'userManagement.lastName' },
      {
        name: 'enabled',
        type: NglFilterFieldType.SELECT,
        translation: 'userManagement.activated',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
    ];
  }

  openUpdate(user?: User, duplicate?: boolean): void {
    const options: EntityUpdateOptions<UserUpdateModalComponent> = {
      component: UserUpdateModalComponent,
      componentInstances: {
        data: user || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  private createOptions(user: User): EntityViewOptions[] {
    const div = document.createElement('div');
    user.groups?.forEach(group => {
      const span: HTMLSpanElement = document.createElement('span');
      span.className = 'badge badge-info ml-2';
      span.innerText = group;

      div.appendChild(span);
    });

    const options: EntityViewOptions[] = [
      { title: 'userManagement.login', value: user.username },
      { title: 'global.title', value: div },
      { title: 'userManagement.firstName', value: user.firstName },
      { title: 'userManagement.lastName', value: user.lastName },
      { title: 'userManagement.email', value: user.email },
      { title: 'userManagement.activated', value: user.enabled, type: 'boolean', boolean: 'active' },
      { title: 'userManagement.createdBy', value: user.createdBy },
      { title: 'userManagement.createdDate', value: user.createdDate, type: 'date' },
      { title: 'userManagement.lastModifiedBy', value: user.lastModifiedBy },
      { title: 'userManagement.lastModifiedDate', value: user.lastModifiedDate, type: 'date' },
    ];

    return options;
  }

  openView(user: User): void {
    this.groupService
      .byUser(user.id)
      .pipe(
        finalize(() => {
          this.entityService.view(this.createOptions(user), user.id);
        })
      )
      .subscribe(res => (user.groups = res.body?.map(it => it.name)));
  }

  openDelete(id = null, login?: string): void {
    const options = {
      useFunction: this.delete(login),
      event: Event.USER_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsUser.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'User',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsUser',
    };

    this.entityService.history(options);
  }
}
