import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {GatewayParam} from './gateway-param.model';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {RecipientService} from '../recipient/recipient.service';
import {EntityService} from '../../../shared/entity/entity.service';
import {GatewayParamUpdateModalComponent} from './gateway-param-update-modal.component';
import {EnumerationService} from '../../../shared/service/enumeration/enumeration.service';
import {RequestType} from '../../../shared/model/enumerations/request-type.model';
import {ProtocolType} from '../../../shared/model/enumerations/protocol-type.model';
import {ModeType} from '../../../shared/model/enumerations/mode-type.model';

type EntityResponseType = HttpResponse<GatewayParam>;
type EntityArrayResponseType = HttpResponse<GatewayParam[]>;

@Injectable({ providedIn: 'root' })
export class GatewayParamService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/gateway-params', 'bmms');

  constructor(
    private http: HttpClient,
    private entityService: EntityService,
    private recipientService: RecipientService,
    private enumService: EnumerationService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(gatewayParam: GatewayParam): Observable<EntityResponseType> {
    return this.http.post<GatewayParam>(this.resourceUrl, gatewayParam, { observe: 'response' });
  }

  update(gatewayParam: GatewayParam): Observable<EntityResponseType> {
    return this.http.put<GatewayParam>(`${this.resourceUrl}/${gatewayParam.id}`, gatewayParam, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<GatewayParam>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<GatewayParam[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      { name: 'url.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsGatewayParam.url' },
      {
        name: 'type.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsGatewayParam.type',
        options: {
          'select.items': this.enumService.getOnlyValues('RequestType', RequestType),
        },
      },
      {
        name: 'username.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsGatewayParam.username',
      },
      {
        name: 'recipientId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsGatewayParam.recipient',
        options: {
          'select.resourceUrl': this.recipientService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'protocol.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsGatewayParam.protocol',
        options: {
          'select.items': this.enumService.getOnlyValues('ProtocolType', ProtocolType),
        },
      },
      {
        name: 'mode.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsGatewayParam.mode',
        options: {
          'select.items': this.enumService.getOnlyValues('ModeType', ModeType),
        },
      },
      {
        name: 'active.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsGatewayParam.active',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
    ];
  }

  openUpdate(gatewayParam?: GatewayParam, duplicate?: boolean): void {
    const options: EntityUpdateOptions<GatewayParamUpdateModalComponent> = {
      component: GatewayParamUpdateModalComponent,
      componentInstances: {
        data: gatewayParam || {},
        duplicate: duplicate
      },
      modalOptions: { size: 'lg' },
    };

    this.entityService.update(options);
  }

  openView(gatewayParam: GatewayParam): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: gatewayParam.id },
      { title: 'billMasterGwApp.pkbillmsGatewayParam.url', value: gatewayParam.url },
      { title: 'billMasterGwApp.pkbillmsGatewayParam.type', value: gatewayParam.type },
      { title: 'billMasterGwApp.pkbillmsGatewayParam.username', value: gatewayParam.username },
      {
        title: 'billMasterGwApp.pkbillmsGatewayParam.recipient',
        value: gatewayParam.recipient?.shortName,
        type: 'link',
        link: () => this.recipientService.openView(gatewayParam.recipient),
      },
      { title: 'billMasterGwApp.pkbillmsGatewayParam.protocol', value: gatewayParam.protocol },
      {
        title: 'billMasterGwApp.pkbillmsGatewayParam.active',
        value: gatewayParam.active,
        type: 'boolean',
        boolean: 'active',
      },
      { title: 'billMasterGwApp.pkbillmsGatewayParam.mode', value: gatewayParam.mode },
    ];

    this.entityService.view(options, gatewayParam.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.GATEWAY_PARAM_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsGatewayParam.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'GatewayParam',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsGatewayParam',
    };

    this.entityService.history(options);
  }
}
