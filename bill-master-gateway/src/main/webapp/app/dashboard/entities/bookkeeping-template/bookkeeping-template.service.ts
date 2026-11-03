import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BookkeepingAmountCalculateType, BookkeepingTemplate} from './bookkeeping-template.model';
import {createRequestOption} from '../../../core/util/request-util';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {GatewayService} from '../gateway/gateway.service';
import {RecipientService} from '../recipient/recipient.service';
import {BookkeepingTemplateUpdateModalComponent} from './bookkeeping-template-update-modal.component';
import {EntityService} from '../../../shared/entity/entity.service';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EnumerationService} from '../../../shared/service/enumeration/enumeration.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {BktPeriod} from '../../../shared/model/enumerations/bkt-period.model';
import {PayType} from '../../../shared/model/enumerations/pay-type.model';
import {BookkeepingTransactionType} from '../../../shared/model/enumerations/bookkeeping-transaction-type.model';

type EntityResponseType = HttpResponse<BookkeepingTemplate>;
type EntityArrayResponseType = HttpResponse<BookkeepingTemplate[]>;

@Injectable({ providedIn: 'root' })
export class BookkeepingTemplateService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/bookkeeping-templates', 'bmms');

  constructor(
    protected http: HttpClient,
    private gatewayService: GatewayService,
    private recipientService: RecipientService,
    private entityService: EntityService,
    private enumService: EnumerationService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(bookkeepingTemplate: BookkeepingTemplate): Observable<EntityResponseType> {
    return this.http.post<BookkeepingTemplate>(this.resourceUrl, bookkeepingTemplate, { observe: 'response' });
  }

  update(bookkeepingTemplate: BookkeepingTemplate): Observable<EntityResponseType> {
    return this.http.put<BookkeepingTemplate>(`${this.resourceUrl}/${bookkeepingTemplate.id}`, bookkeepingTemplate, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<BookkeepingTemplate>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<BookkeepingTemplate[]>(this.resourceUrl, { params: options, observe: 'response' });
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
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.name',
      },
      {
        name: 'sMfo.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.sMfo',
      },
      {
        name: 'sInn.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.sInn',
      },
      {
        name: 'sAccount.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.sAccount',
      },
      {
        name: 'rMfo.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.rMfo',
      },
      {
        name: 'rInn.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.rInn',
      },
      {
        name: 'rAccount.contains',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.rAccount',
      },
      {
        name: 'calculateAmountRule.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.calculateAmountRule',
      },
      {
        name: 'period.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.period',
        options: {
          'select.items': this.enumService.getOnlyValues('BktPeriod', BktPeriod),
        },
      },
      {
        name: 'payTypes.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.payTypes',
        options: {
          'select.items': this.enumService.getOnlyValues('PayType', PayType),
        },
      },
      {
        name: 'bookkeepingType.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.bookkeepingType',
        options: {
          'select.items': this.enumService.getOnlyValues('BookkeepingTransactionType', BookkeepingTransactionType),
        },
      },
      {
        name: 'amountCalculateType.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.amountCalculateType',
        options: {
          'select.items': this.enumService.getOnlyValues('BookkeepingAmountCalculateType', BookkeepingAmountCalculateType),
        },
      },
      {
        name: 'gatewayId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.gateway',
        options: {
          'select.resourceUrl': this.gatewayService.resourceUrl,
          'select.nzLabel': 'name',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'recipientId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsBookkeepingTemplate.recipient',
        options: {
          'select.resourceUrl': this.recipientService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
    ];
  }

  openUpdate(bookkeepingTemplate?: BookkeepingTemplate, duplicate?: boolean): void {
    const options: EntityUpdateOptions<BookkeepingTemplateUpdateModalComponent> = {
      component: BookkeepingTemplateUpdateModalComponent,
      componentInstances: {
        data: bookkeepingTemplate || new BookkeepingTemplate(),
        duplicate: duplicate
      },
      modalOptions: { backdrop: 'static', size: 'xl' },
    };

    this.entityService.update(options);
  }

  openView(bookkeepingTemplate: BookkeepingTemplate): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: bookkeepingTemplate.id },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.name', value: bookkeepingTemplate.name },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.sMfo', value: bookkeepingTemplate.sMfo },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.sInn', value: bookkeepingTemplate.sInn },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.sAccount', value: bookkeepingTemplate.sAccount },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.rMfo', value: bookkeepingTemplate.rMfo },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.rInn', value: bookkeepingTemplate.rInn },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.rAccount', value: bookkeepingTemplate.rAccount },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.calculateAmountRule',
        value: bookkeepingTemplate.calculateAmountRule,
      },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.period', value: bookkeepingTemplate.period },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.payTypes', value: bookkeepingTemplate.payTypes },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.desc', value: bookkeepingTemplate.desc },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.sType', value: bookkeepingTemplate.sType },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.sTypeIds', value: bookkeepingTemplate.sTypeIds },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.bookkeepingType',
        value: bookkeepingTemplate.bookkeepingType,
      },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.amountCalculateType',
        value: bookkeepingTemplate.amountCalculateType,
      },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.gateway',
        value: bookkeepingTemplate.gateway?.name,
        type: 'link',
        link: () => this.gatewayService.openView(bookkeepingTemplate.gateway),
      },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.recipient',
        value: bookkeepingTemplate.recipient?.shortName,
        type: 'link',
        link: () => this.recipientService.openView(bookkeepingTemplate.recipient),
      },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.docType', value: bookkeepingTemplate.docType },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.docCode', value: bookkeepingTemplate.docCode },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.docTaskCode', value: bookkeepingTemplate.docTaskCode },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.rAccountName', value: bookkeepingTemplate.rAccountName },
      {
        title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.bookkeepingPayType',
        value: bookkeepingTemplate.bookkeepingPayType,
      },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.groupIds', value: bookkeepingTemplate.groupIds },
      { title: 'billMasterGwApp.pkbillmsBookkeepingTemplate.control', value: bookkeepingTemplate.control },
    ];

    this.entityService.view(options, bookkeepingTemplate.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.BOOKKEEPING_TEMPLATE_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsBookkeepingTemplate.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'BookkeepingTemplate',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsBookkeepingTemplate',
    };

    this.entityService.history(options);
  }
}
