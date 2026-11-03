import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../../core/util/request-util';
import {ApplicationConfigService} from '../../../../core/config/application-config.service';
import {IEntityConfig} from '../../../../shared/entity/entity-config.model';
import {EntityService} from '../../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../../config/event.constants';
import {P2pCommissionUpdateModalComponent} from './p2p-commission-update-modal.component';
import {P2pCommission} from './p2p-commission.model';
import {Authority} from "../../../../config/authority.constants";

type EntityArrayResponseType = HttpResponse<P2pCommission[]>;

@Injectable({ providedIn: 'root' })
export class P2pCommissionService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/p-2-p-commissions', 'bmms');

  constructor(private http: HttpClient, private entityService: EntityService, private applicationConfigService: ApplicationConfigService) {}

  create(p2pCommission: P2pCommission): Observable<P2pCommission> {
    return this.http.post<P2pCommission>(this.resourceUrl, p2pCommission, { observe: 'body' });
  }

  update(p2pCommission: P2pCommission): Observable<P2pCommission> {
    return this.http.put<P2pCommission>(`${this.resourceUrl}/${p2pCommission.id}`, p2pCommission, { observe: 'body' });
  }

  find(id: number): Observable<P2pCommission> {
    return this.http.get<P2pCommission>(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<P2pCommission[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'name.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsP2PCommission.name' },
      { name: 'fee.equals', type: NglFilterFieldType.NUMBER, translation: 'billMasterGwApp.pkbillmsP2PCommission.fee' },
      { name: 'up.equals', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsP2PCommission.up' },
      { name: 'down.equals', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsP2PCommission.down' },
      {
        name: 'description.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsP2PCommission.description',
      },
    ];
  }

  openUpdate(p2pCommission?: P2pCommission, duplicate?: boolean): void {
    const options: EntityUpdateOptions<P2pCommissionUpdateModalComponent> = {
      component: P2pCommissionUpdateModalComponent,
      componentInstances: {
        data: p2pCommission || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(p2pCommission: P2pCommission): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: p2pCommission.id },
      { title: 'billMasterGwApp.pkbillmsP2PCommission.name', value: p2pCommission.name },
      { title: 'billMasterGwApp.pkbillmsP2PCommission.fee', value: p2pCommission.fee },
      { title: 'billMasterGwApp.pkbillmsP2PCommission.up', value: p2pCommission.up },
      { title: 'billMasterGwApp.pkbillmsP2PCommission.down', value: p2pCommission.down },
      { title: 'billMasterGwApp.pkbillmsP2PCommission.description', value: p2pCommission.description },
    ];

    this.entityService.view(options, p2pCommission.id, [Authority.P2P_COMMISSION_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.P2P_COMMISSION_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsP2PCommission.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'P2PCommission',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsP2PCommission',
    };

    this.entityService.history(options);
  }
}
