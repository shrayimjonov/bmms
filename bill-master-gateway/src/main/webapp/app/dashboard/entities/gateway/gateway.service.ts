import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {Gateway} from './gateway.model';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {EntityService} from '../../../shared/entity/entity.service';
import {GatewayUpdateModalComponent} from './gateway-update-modal.component';

type EntityResponseType = HttpResponse<Gateway>;
type EntityArrayResponseType = HttpResponse<Gateway[]>;

@Injectable({ providedIn: 'root' })
export class GatewayService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/gateways', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(gateway: Gateway): Observable<EntityResponseType> {
    return this.http.post<Gateway>(this.resourceUrl, gateway, { observe: 'response' });
  }

  update(gateway: Gateway): Observable<EntityResponseType> {
    return this.http.put<Gateway>(`${this.resourceUrl}/${gateway.id}`, gateway, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<Gateway>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<Gateway[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/cascaded/${id}`, { observe: 'response' });
  }

  getSpringBeanNames(): Observable<string[]> {
    return this.http.get<string[]>(this.applicationConfigService.getEndpointFor('api/spring-bean-names/list', 'bmms'), { observe: 'body' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      { name: 'name.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsGateway.name' },
      { name: 'org.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsGateway.org' },
      { name: 'taxnr.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsGateway.taxnr',
        options: {
          mask: '000000000'
        }
      },
      {
        name: 'springBeanName.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsGateway.springBeanName',
        options: {
          'select.resourceUrl': this.applicationConfigService.getEndpointFor('api/spring-bean-names/list', 'bmms'),
        },
      },
      {
        name: 'active.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsGateway.active',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
    ];
  }

  openUpdate(gateway?: Gateway, duplicate?: boolean): void {
    const options: EntityUpdateOptions<GatewayUpdateModalComponent> = {
      component: GatewayUpdateModalComponent,
      componentInstances: {
        data: gateway || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(gateway: Gateway): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: gateway.id },
      { title: 'billMasterGwApp.pkbillmsGateway.name', value: gateway.name },
      { title: 'billMasterGwApp.pkbillmsGateway.org', value: gateway.org },
      { title: 'billMasterGwApp.pkbillmsGateway.mfo', value: gateway.mfo },
      { title: 'billMasterGwApp.pkbillmsGateway.ban', value: gateway.ban },
      { title: 'billMasterGwApp.pkbillmsGateway.taxnr', value: gateway.taxnr },
      { title: 'billMasterGwApp.pkbillmsGateway.contract', value: gateway.contract },
      { title: 'billMasterGwApp.pkbillmsGateway.springBeanName', value: gateway.springBeanName },
      { title: 'billMasterGwApp.pkbillmsGateway.active', value: gateway.active, type: 'boolean', boolean: 'active' },
    ];

    this.entityService.view(options, gateway.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.GATEWAY_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsGateway.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Gateway',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsGateway',
    };

    this.entityService.history(options);
  }
}
