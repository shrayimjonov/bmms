import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ControlType, Field} from './field.model';
import {createRequestOption} from '../../../core/util/request-util';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityService} from '../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {RecipientService} from '../recipient/recipient.service';
import {FieldUpdateModalComponent} from './field-update-modal.component';
import {FieldType} from '../../../shared/model/enumerations/field-type.model';
import {RequestType} from '../../../shared/model/enumerations/request-type.model';
import {EnumerationService} from '../../../shared/service/enumeration/enumeration.service';
import {Authority} from "../../../config/authority.constants";

@Injectable({ providedIn: 'root' })
export class FieldService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/fields', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private recipientService: RecipientService,
    private enumService: EnumerationService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(field: Field): Observable<Field> {
    return this.http.post<Field>(this.resourceUrl, field, { observe: 'body' });
  }

  updateList(fields: Field[]): Observable<Field[]> {
    return this.http.post<Field[]>(this.resourceUrl + '/update-list', fields, { observe: 'body' });
  }

  update(field: Field): Observable<Field> {
    return this.http.put<Field>(`${this.resourceUrl}/${field.id}`, field, { observe: 'body' });
  }

  find(id: number): Observable<Field> {
    return this.http.get<Field>(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<Field[]>> {
    const options = createRequestOption(req);
    return this.http.get<Field[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<{}> {
    return this.http.delete(`${this.resourceUrl}/cascaded/${id}`, { observe: 'body' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      { name: 'name.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsField.name' },
      {
        name: 'recipientId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsField.recipient',
        options: {
          'select.resourceUrl': this.recipientService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'type.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsField.type',
        options: {
          'select.items': this.enumService.getOnlyValues('FieldType', FieldType),
        },
      },
      { name: 'ord.equals', type: NglFilterFieldType.NUMBER, translation: 'billMasterGwApp.pkbillmsField.ord' },
      {
        name: 'controlType.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsField.controlType',
        options: {
          'select.items': this.enumService.getOnlyValues('ControlType', ControlType),
        },
      },
      {
        name: 'requestType.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsField.requestType',
        options: {
          'select.items': this.enumService.getOnlyValues('RequestType', RequestType),
        },
      },
    ];
  }

  openUpdate(field?: Field, duplicate?: boolean): void {
    const options: EntityUpdateOptions<FieldUpdateModalComponent> = {
      component: FieldUpdateModalComponent,
      componentInstances: {
        data: field || {},
        duplicate: duplicate
      },
      modalOptions: { size: 'lg' },
    };

    this.entityService.update(options);
  }

  openView(field: Field): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: field.id },
      { title: 'billMasterGwApp.pkbillmsField.name', value: `${field.name} - ${field.recipient?.shortName}` },
      {
        title: 'billMasterGwApp.pkbillmsField.recipient',
        value: field.recipient?.shortName,
        type: 'link',
        link: () => this.recipientService.openView(field.recipient),
      },
      { title: 'billMasterGwApp.pkbillmsField.type', value: field.type },
      { title: 'billMasterGwApp.pkbillmsField.validation', value: field.validation },
      { title: 'billMasterGwApp.pkbillmsField.lengthMin', value: field.lengthMin },
      { title: 'billMasterGwApp.pkbillmsField.lengthMax', value: field.lengthMax },
      { title: 'billMasterGwApp.pkbillmsField.requireField', value: field.requireField },
      { title: 'billMasterGwApp.pkbillmsField.optional', value: field.optional },
      { title: 'billMasterGwApp.pkbillmsField.ord', value: field.ord },
      { title: 'billMasterGwApp.pkbillmsField.controlType', value: field.controlType },
      { title: 'billMasterGwApp.pkbillmsField.requestType', value: field.requestType },
    ];

    this.entityService.view(options, field.id, [Authority.FIELD_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.FIELD_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsField.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Field',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsField',
    };

    this.entityService.history(options);
  }
}
