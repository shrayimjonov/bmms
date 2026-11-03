import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {Event} from '../../../config/event.constants';
import {EntityService} from '../../../shared/entity/entity.service';
import {FieldService} from '../field/field.service';
import {Prefix} from './prefix.model';
import {PrefixUpdateModalComponent} from './prefix-update-modal.component';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {EnumerationService} from '../../../shared/service/enumeration/enumeration.service';
import {PrefixType} from '../../../shared/model/enumerations/prefix-type.model';
import {Field} from "../field/field.model";

type EntityResponseType = HttpResponse<Prefix>;
type EntityArrayResponseType = HttpResponse<Prefix[]>;

@Injectable({ providedIn: 'root' })
export class PrefixService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/prefixes', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private fieldService: FieldService,
    private enumService: EnumerationService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(prefix: Prefix): Observable<EntityResponseType> {
    return this.http.post<Prefix>(this.resourceUrl, prefix, { observe: 'response' });
  }

  update(prefix: Prefix): Observable<EntityResponseType> {
    return this.http.put<Prefix>(`${this.resourceUrl}/${prefix.id}`, prefix, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<Prefix>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<Prefix[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getRecipientWithFieldColText(field: Field): string {
    if (field.recipient?.shortName) {
      return `${field.recipient.shortName} - ${field.name}`;
    }

    return field.name;
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'fieldId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsPrefix.field',
        options: {
          'select.resourceUrl': this.fieldService.resourceUrl,
          'select.nzLabel': 'name',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'type.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsPrefix.type',
        options: {
          'select.items': this.enumService.getOnlyValues('PrefixType', PrefixType),
        },
      },
      { name: 'value.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsPrefix.value' },
      {
        name: 'requireField.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsPrefix.requireField',
      },
    ];
  }

  openUpdate(prefix?: Prefix, duplicate?: boolean): void {
    const options: EntityUpdateOptions<PrefixUpdateModalComponent> = {
      component: PrefixUpdateModalComponent,
      componentInstances: {
        data: prefix || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(prefix: Prefix): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: prefix.id },
      {
        title: 'billMasterGwApp.pkbillmsPrefix.field',
        value: this.getRecipientWithFieldColText(prefix.field),
        type: 'link',
        link: () => this.fieldService.openView(prefix.field),
      },
      { title: 'billMasterGwApp.pkbillmsPrefix.type', value: prefix.type },
      { title: 'billMasterGwApp.pkbillmsPrefix.value', value: prefix.value },
      { title: 'billMasterGwApp.pkbillmsPrefix.requireField', value: prefix.requireField },
    ];

    this.entityService.view(options, prefix.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.PREFIX_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsPrefix.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Prefix',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsPrefix',
    };

    this.entityService.history(options);
  }
}
