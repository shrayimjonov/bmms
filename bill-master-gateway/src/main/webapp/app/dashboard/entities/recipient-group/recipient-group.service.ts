import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {RecipientGroup} from './recipient-group.model';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityService} from '../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {Event} from '../../../config/event.constants';
import {RecipientGroupUpdateModalComponent} from './recipient-group-update-modal.component';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {RecipientService} from '../recipient/recipient.service';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';

type EntityResponseType = HttpResponse<RecipientGroup>;
type EntityArrayResponseType = HttpResponse<RecipientGroup[]>;

@Injectable({ providedIn: 'root' })
export class RecipientGroupService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/recipient-groups', 'bmms');

  constructor(
    private http: HttpClient,
    private entityService: EntityService,
    private recipientService: RecipientService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(recipientGroup: RecipientGroup): Observable<EntityResponseType> {
    return this.http.post<RecipientGroup>(this.resourceUrl, recipientGroup, { observe: 'response' });
  }

  update(recipientGroup: RecipientGroup): Observable<EntityResponseType> {
    return this.http.put<RecipientGroup>(`${this.resourceUrl}/${recipientGroup.id}`, recipientGroup, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<RecipientGroup>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RecipientGroup[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'name.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsRecipientGroup.name',
      },
      {
        name: 'more.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsRecipientGroup.more',
      },
      {
        name: 'active.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsRecipientGroup.active',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
    ];
  }

  openUpdate(recipientGroup?: RecipientGroup, duplicate?: boolean): void {
    const options: EntityUpdateOptions<RecipientGroupUpdateModalComponent> = {
      component: RecipientGroupUpdateModalComponent,
      componentInstances: {
        data: recipientGroup || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(recipientGroup: RecipientGroup): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: recipientGroup.id },
      { title: 'billMasterGwApp.pkbillmsRecipientGroup.name', value: recipientGroup.name },
      {
        title: 'billMasterGwApp.pkbillmsRecipientGroup.recipient',
        value: recipientGroup.recipients.map(r => r.shortName),
        type: 'link',
        link: (value: any) => this.recipientService.openView(recipientGroup.recipients.find(recipient => recipient.shortName === value)),
      },
      { title: 'billMasterGwApp.pkbillmsRecipientGroup.more', value: recipientGroup.more },
      {
        title: 'billMasterGwApp.pkbillmsRecipientGroup.active',
        value: recipientGroup.active,
        type: 'boolean',
        boolean: 'active',
      },
    ];

    this.entityService.view(options, recipientGroup.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.RECIPIENT_GROUP_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsRecipientGroup.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'RecipientGroup',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsRecipientGroup',
    };

    this.entityService.history(options);
  }
}
