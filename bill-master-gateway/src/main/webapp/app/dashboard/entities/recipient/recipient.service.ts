import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recipient, RecipientType } from './recipient.model';
import { createRequestOption } from '../../../core/util/request-util';
import { ApplicationConfigService } from '../../../core/config/application-config.service';
import { NglFilterField, NglFilterFieldType } from 'ngl-filter-field';
import { Event } from '../../../config/event.constants';
import { IEntityConfig } from '../../../shared/entity/entity-config.model';
import { RecipientUpdateModalComponent } from './recipient-update-modal.component';
import { EntityService } from '../../../shared/entity/entity.service';
import { EntityViewOptions } from '../../../shared/entity/entity-view/entity-view-options.model';
import { EntityUpdateOptions } from '../../../shared/entity/entity-update/entity-update-options.model';
import { EnumerationService } from '../../../shared/service/enumeration/enumeration.service';
import { Authority } from '../../../config/authority.constants';

@Injectable({ providedIn: 'root' })
export class RecipientService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/recipients', 'bmms');

  constructor(
    private readonly http: HttpClient,
    private readonly entityService: EntityService,
    private readonly enumService: EnumerationService,
    private readonly applicationConfigService: ApplicationConfigService
  ) {}

  create(recipient: Recipient): Observable<HttpResponse<Recipient>> {
    return this.http.post<Recipient>(this.resourceUrl, recipient, { observe: 'response' });
  }

  update(recipient: Recipient): Observable<HttpResponse<Recipient>> {
    return this.http.put<Recipient>(`${this.resourceUrl}/${recipient.id}`, recipient, { observe: 'response' });
  }

  query(req?: any): Observable<HttpResponse<Recipient[]>> {
    return this.http.get<Recipient[]>(this.resourceUrl, { params: createRequestOption(req), observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/cascaded/${id}`, { observe: 'response' });
  }

  find(id: number): Observable<HttpResponse<Recipient>> {
    return this.http.get<Recipient>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'shortName.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsRecipient.shortName',
      },
      {
        name: 'logo.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsRecipient.logo',
      },
      {
        name: 'serviceId.equals',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsRecipient.serviceId',
      },
      {
        name: 'type.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsRecipient.type',
        options: {
          'select.items': this.enumService.getOnlyValues('RecipientType', RecipientType),
        },
      },
      {
        name: 'minAmount.greaterThanOrEqual',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsRecipient.minAmount',
      },
      {
        name: 'maxAmount.lessThanOrEqual',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsRecipient.maxAmount',
      },
      {
        name: 'limitRatio.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsRecipient.limitRatio',
      },
      {
        name: 'amountRatio.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsRecipient.amountRatio',
      },
      {
        name: 'active.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsRecipient.active',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
    ];
  }

  openUpdate(recipient?: Recipient, duplicate?: boolean): void {
    const options: EntityUpdateOptions<RecipientUpdateModalComponent> = {
      component: RecipientUpdateModalComponent,
      componentInstances: {
        data: recipient || {},
        duplicate: duplicate,
      },
      modalOptions: { size: 'lg' },
    };

    this.entityService.update(options);
  }

  openView(recipient: Recipient): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: recipient.id },
      { title: 'billMasterGwApp.pkbillmsRecipient.shortName', value: recipient.shortName },
      { title: 'billMasterGwApp.pkbillmsRecipient.serviceId', value: recipient.serviceId },
      {
        title: 'billMasterGwApp.pkbillmsRecipient.amountRatio',
        value: recipient.amountRatio,
        type: 'translation',
        translation: 'global.amountRatio.' + recipient.amountRatio,
      },
      { title: 'billMasterGwApp.pkbillmsRecipient.ccy', value: recipient.ccy },
      { title: 'billMasterGwApp.pkbillmsRecipient.minAmount', value: recipient.minAmount },
      { title: 'billMasterGwApp.pkbillmsRecipient.maxAmount', value: recipient.maxAmount },
      {
        title: 'billMasterGwApp.pkbillmsRecipient.limitRatio',
        value: recipient.limitRatio,
        type: 'translation',
        translation: 'global.amountRatio.' + recipient.limitRatio,
      },
      { title: 'billMasterGwApp.pkbillmsRecipient.active', value: recipient.active, type: 'boolean', boolean: 'active' },
      { title: 'billMasterGwApp.pkbillmsRecipient.mfo', value: recipient.mfo },
      { title: 'billMasterGwApp.pkbillmsRecipient.inn', value: recipient.inn },
      { title: 'billMasterGwApp.pkbillmsRecipient.type', value: recipient.type },
      {
        title: 'billMasterGwApp.pkbillmsAgentRecipient.jsonParams',
        value: JSON.parse(recipient.jsonParams),
        type: 'json',
      },
    ];

    this.entityService.view(options, recipient.id, [Authority.RECIPIENT_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.RECIPIENT_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsRecipient.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Recipient',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsRecipient',
    };

    this.entityService.history(options);
  }
}
