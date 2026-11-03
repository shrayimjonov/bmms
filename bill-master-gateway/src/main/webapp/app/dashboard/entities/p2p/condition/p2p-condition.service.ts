import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../../core/util/request-util';
import {P2pCondition} from './p2p-condition.model';
import {ApplicationConfigService} from '../../../../core/config/application-config.service';
import {IEntityConfig} from '../../../../shared/entity/entity-config.model';
import {EntityService} from '../../../../shared/entity/entity.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {EntityUpdateOptions} from '../../../../shared/entity/entity-update/entity-update-options.model';
import {EntityViewOptions} from '../../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../../config/event.constants';
import {P2pConditionUpdateModalComponent} from './p2p-condition-update-modal.component';
import {Authority} from "../../../../config/authority.constants";

@Injectable({ providedIn: 'root' })
export class P2pConditionService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/p-2-p-conditions', 'bmms');

  constructor(private http: HttpClient, private entityService: EntityService, private applicationConfigService: ApplicationConfigService) {}

  create(p2pCondition: P2pCondition): Observable<P2pCondition> {
    return this.http.post<P2pCondition>(this.resourceUrl, p2pCondition, { observe: 'body' });
  }

  update(p2pCondition: P2pCondition): Observable<P2pCondition> {
    return this.http.put<P2pCondition>(`${this.resourceUrl}/${p2pCondition.id}`, p2pCondition, { observe: 'body' });
  }

  find(id: number): Observable<P2pCondition> {
    return this.http.get<P2pCondition>(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<P2pCondition[]>> {
    const options = createRequestOption(req);
    return this.http.get<P2pCondition[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'value.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsP2PCondition.value',
      },
      {
        name: 'description.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsP2PCondition.description',
      },
    ];
  }

  openUpdate(p2pCondition?: P2pCondition, duplicate?: boolean): void {
    const options: EntityUpdateOptions<P2pConditionUpdateModalComponent> = {
      component: P2pConditionUpdateModalComponent,
      componentInstances: {
        data: p2pCondition || {},
        duplicate: duplicate
      },
    };

    this.entityService.update(options);
  }

  openView(p2pCondition: P2pCondition): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: p2pCondition.id },
      { title: 'billMasterGwApp.pkbillmsP2PCondition.value', value: p2pCondition.value },
      { title: 'billMasterGwApp.pkbillmsP2PCondition.description', value: p2pCondition.description },
    ];

    this.entityService.view(options, p2pCondition.id, [Authority.P2P_CONDITION_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.P2P_CONDITION_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsP2PCondition.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'P2PCondition',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsP2PCondition',
    };

    this.entityService.history(options);
  }
}
