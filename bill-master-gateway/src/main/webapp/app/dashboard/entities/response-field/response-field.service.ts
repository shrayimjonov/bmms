import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {ResponseField, ResponseFieldType} from './response-field.model';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityService} from '../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {RecipientService} from '../recipient/recipient.service';
import {ResponseFieldUpdateModalComponent} from './response-field-update-modal.component';
import {EnumerationService} from '../../../shared/service/enumeration/enumeration.service';
import {Visibility} from '../../../shared/model/enumerations/visibility.model';
import {RequestType} from '../../../shared/model/enumerations/request-type.model';
import {Authority} from "../../../config/authority.constants";

type EntityResponseType = HttpResponse<ResponseField>;
type EntityArrayResponseType = HttpResponse<ResponseField[]>;

@Injectable({ providedIn: 'root' })
export class ResponseFieldService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/response-fields', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private enumerationService: EnumerationService,
    private recipientService: RecipientService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(responseField: ResponseField): Observable<EntityResponseType> {
    return this.http.post<ResponseField>(this.resourceUrl, responseField, { observe: 'response' });
  }

  update(responseField: ResponseField): Observable<EntityResponseType> {
    return this.http.put<ResponseField>(`${this.resourceUrl}/${responseField.id}`, responseField, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ResponseField>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ResponseField[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'recipientId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsResponseField.recipientId',
        options: {
          'select.resourceUrl': this.recipientService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
      { name: 'name.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsResponseField.name' },
      {
        name: 'visibility.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsResponseField.visibility',
        options: {
          'select.items': this.enumerationService.getOnlyValues('Visibility', Visibility),
        },
      },
      { name: 'ord.equals', type: NglFilterFieldType.NUMBER, translation: 'billMasterGwApp.pkbillmsResponseField.ord' },
      {
        name: 'requestType.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsResponseField.requestType',
        options: {
          'select.items': this.enumerationService.getOnlyValues('RequestType', RequestType),
        },
      },
      {
        name: 'type.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsResponseField.type',
        options: {
          'select.items': this.enumerationService.getOnlyValues('ResponseFieldType', ResponseFieldType),
        },
      },
    ];
  }

  openUpdate(responseField?: ResponseField, duplicate?: boolean): void {
    const options: EntityUpdateOptions<ResponseFieldUpdateModalComponent> = {
      component: ResponseFieldUpdateModalComponent,
      componentInstances: {
        data: responseField || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(responseField: ResponseField): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: responseField.id },
      { title: 'billMasterGwApp.pkbillmsResponseField.name', value: responseField.name },
      { title: 'billMasterGwApp.pkbillmsResponseField.visibility', value: responseField.visibility },
      { title: 'billMasterGwApp.pkbillmsResponseField.ord', value: responseField.ord },
      { title: 'billMasterGwApp.pkbillmsResponseField.requestType', value: responseField.requestType },
      {
        title: 'billMasterGwApp.pkbillmsResponseField.recipientId',
        value: responseField.recipient?.shortName,
        type: 'link',
        link: () => this.recipientService.openView(responseField.recipient),
      },
      { title: 'billMasterGwApp.pkbillmsResponseField.type', value: responseField.type },
    ];

    this.entityService.view(options, responseField.id, [Authority.RESPONSE_FIELD_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.RESPONSE_FIELD_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsResponseField.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'ResponseField',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsResponseField',
    };

    this.entityService.history(options);
  }
}
