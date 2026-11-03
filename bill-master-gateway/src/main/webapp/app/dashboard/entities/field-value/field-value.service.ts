import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {FieldValue} from './field-value.model';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityService} from '../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {FieldService} from '../field/field.service';
import {FieldValueUpdateModalComponent} from './field-value-update-modal.component';

type EntityResponseType = HttpResponse<FieldValue>;
type EntityArrayResponseType = HttpResponse<FieldValue[]>;

@Injectable({ providedIn: 'root' })
export class FieldValueService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/field-values', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private fieldService: FieldService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(fieldValue: FieldValue): Observable<EntityResponseType> {
    return this.http.post<FieldValue>(this.resourceUrl, fieldValue, { observe: 'response' });
  }

  update(fieldValue: FieldValue): Observable<EntityResponseType> {
    return this.http.put<FieldValue>(`${this.resourceUrl}/${fieldValue.id}`, fieldValue, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<FieldValue>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<FieldValue[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/cascaded/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'fieldName.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsFieldValue.field',
      },
      { name: 'value.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsFieldValue.value' },
      {
        name: 'amount.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsFieldValue.amount',
      },
    ];
  }

  openUpdate(fieldValue?: FieldValue, duplicate?: boolean): void {
    const options: EntityUpdateOptions<FieldValueUpdateModalComponent> = {
      component: FieldValueUpdateModalComponent,
      componentInstances: {
        data: fieldValue || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(fieldValue: FieldValue): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: fieldValue.id },
      {
        title: 'billMasterGwApp.pkbillmsFieldValue.field',
        value: fieldValue.field?.name,
        type: 'link',
        link: () => this.fieldService.openView(fieldValue.field),
      },
      { title: 'billMasterGwApp.pkbillmsFieldValue.value', value: fieldValue.value },
      { title: 'billMasterGwApp.pkbillmsFieldValue.amount', value: fieldValue.amount },
      { title: 'billMasterGwApp.pkbillmsFieldValue.filterBy', value: fieldValue.filterBy },
      { title: 'billMasterGwApp.pkbillmsFieldValue.ord', value: fieldValue.ord },
    ];

    this.entityService.view(options, fieldValue.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.FIELD_VALUE_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsFieldValue.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'FieldValue',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsFieldValue',
    };

    this.entityService.history(options);
  }
}
