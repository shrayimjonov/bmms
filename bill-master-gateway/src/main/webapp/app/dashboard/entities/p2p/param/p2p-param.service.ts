import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../../core/util/request-util';
import {P2pParam} from './p2p-param.model';
import {P2pParamTestModel} from './test/p2p-param-test.model';
import {ApplicationConfigService} from '../../../../core/config/application-config.service';
import {IEntityConfig} from '../../../../shared/entity/entity-config.model';
import {EntityService} from '../../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../../config/event.constants';
import {P2pConditionService} from '../condition/p2p-condition.service';
import {P2pCommissionService} from '../commission/p2p-commission.service';
import {P2pParamUpdateModalComponent} from './p2p-param-update-modal.component';
import {P2pParamJsonViewModalComponent} from './json-view/p2p-param-json-view-modal.component';
import {EnumerationService} from '../../../../shared/service/enumeration/enumeration.service';
import {P2pProcessorType} from '../../../../shared/model/enumerations/p2p-processor-type.model';
import {Authority} from "../../../../config/authority.constants";

@Injectable({ providedIn: 'root' })
export class P2pParamService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/p-2-p-params', 'bmms');

  constructor(
    private http: HttpClient,
    private entityService: EntityService,
    private p2pConditionService: P2pConditionService,
    private p2pCommissionService: P2pCommissionService,
    private enumerationService: EnumerationService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(p2pParam: P2pParam): Observable<P2pParam> {
    return this.http.post<P2pParam>(this.resourceUrl, p2pParam, { observe: 'body' });
  }

  update(p2pParam: P2pParam): Observable<P2pParam> {
    return this.http.put<P2pParam>(`${this.resourceUrl}/${p2pParam.id}`, p2pParam, { observe: 'body' });
  }

  find(id: number): Observable<P2pParam> {
    return this.http.get<P2pParam>(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<P2pParam[]>> {
    const options = createRequestOption(req);
    return this.http.get<P2pParam[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  test(request: P2pParamTestModel): Observable<any> {
    return this.http.post<any>(this.resourceUrl + '/info', request, { observe: 'body' });
  }

  getPayTypes(): Observable<string[]> {
    return this.http.get<string[]>(this.resourceUrl + '/types', { observe: 'body' });
  }

  getFilterFields(): NglFilterField[] {
    const senderTypes = this.enumerationService.getOnlyValues('P2pProcessorType', P2pProcessorType);
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      { name: 'name.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsGateway.name' },
      {
        name: 'priority.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsP2PParam.priority',
      },
      { name: 'promo.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsP2PParam.promo' },
      {
        name: 'active.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsP2PParam.active',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
      {
        name: 'senderPc.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsP2PParam.senderPc',
        options: {
          'select.items': senderTypes,
        },
      },
      {
        name: 'recipientPc.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsP2PParam.recipientPc',
        options: {
          'select.items': senderTypes,
        },
      },
      { name: 'value.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsP2PParam.value' },
      {
        name: 'senderConditionId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsP2PParam.senderCondition',
        options: {
          'select.resourceUrl': this.p2pConditionService.resourceUrl,
          'select.nzLabel': 'value',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'recipientConditionId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsP2PParam.recipientCondition',
        options: {
          'select.resourceUrl': this.p2pConditionService.resourceUrl,
          'select.nzLabel': 'value',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'senderPercentId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsP2PParam.senderPercent',
        options: {
          'select.resourceUrl': this.p2pCommissionService.resourceUrl,
          'select.nzLabel': 'name',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'recipientPercentId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsP2PParam.recipientPercent',
        options: {
          'select.resourceUrl': this.p2pCommissionService.resourceUrl,
          'select.nzLabel': 'name',
          'select.nzValue': 'id',
        },
      },
    ];
  }

  openUpdate(p2pParam?: P2pParam, duplicate?: boolean): void {
    const options: EntityUpdateOptions<P2pParamUpdateModalComponent> = {
      component: P2pParamUpdateModalComponent,
      componentInstances: {
        data: p2pParam || {},
        duplicate: duplicate
      },
      modalOptions: { size: 'lg' },
    };

    this.entityService.update(options);
  }

  openView(p2pParam: P2pParam): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: p2pParam.id },
      { title: 'billMasterGwApp.pkbillmsP2PParam.name', value: p2pParam.name },
      { title: 'billMasterGwApp.pkbillmsP2PParam.priority', value: p2pParam.priority },
      { title: 'billMasterGwApp.pkbillmsP2PParam.promo', value: p2pParam.promo },
      { title: 'billMasterGwApp.pkbillmsP2PParam.active', value: p2pParam.active, type: 'boolean', boolean: 'active' },
      { title: 'billMasterGwApp.pkbillmsP2PParam.senderPc', value: p2pParam.senderPc },
      { title: 'billMasterGwApp.pkbillmsP2PParam.recipientPc', value: p2pParam.recipientPc },
      {
        title: 'billMasterGwApp.pkbillmsP2PParam.senderCondition',
        value: p2pParam.senderCondition?.value,
        type: 'link',
        link: () => this.p2pConditionService.openView(p2pParam.senderCondition),
      },
      {
        title: 'billMasterGwApp.pkbillmsP2PParam.recipientCondition',
        value: p2pParam.recipientCondition?.value,
        type: 'link',
        link: () => this.p2pConditionService.openView(p2pParam.recipientCondition),
      },
      {
        title: 'billMasterGwApp.pkbillmsP2PParam.senderPercent',
        value: p2pParam.senderPercent?.name,
        type: 'link',
        link: () => this.p2pCommissionService.openView(p2pParam.senderPercent),
      },
      {
        title: 'billMasterGwApp.pkbillmsP2PParam.recipientPercent',
        value: p2pParam.recipientPercent?.name,
        type: 'link',
        link: () => this.p2pCommissionService.openView(p2pParam.recipientPercent),
      },
      {
        title: 'billMasterGwApp.pkbillmsP2PParam.value',
        value: JSON.parse(p2pParam.value),
        type: 'json',
      },
    ];

    this.entityService.view(options, p2pParam.id,[Authority.P2P_PARAM_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.P2P_PARAM_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsP2PParam.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
      const options = {
      qualifiedName: 'P2PParam',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsP2PParam',
    };

    this.entityService.history(options);
  }

  openJsonViewer(strJson: any): void {
    const options: EntityUpdateOptions<P2pParamJsonViewModalComponent> = {
      component: P2pParamJsonViewModalComponent,
      componentInstances: {
        data: JSON.parse(strJson),
      },
    };

    this.entityService.update(options);
  }
}
