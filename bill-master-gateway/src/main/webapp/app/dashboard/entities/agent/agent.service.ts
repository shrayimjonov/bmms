import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {AgentUpdateModalComponent} from './agent-update-modal.component';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {EntityService} from '../../../shared/entity/entity.service';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {Agent, DetailedAgent} from './agent.model';
import {Authority} from '../../../config/authority.constants';

@Injectable({ providedIn: 'root' })
export class AgentService implements IEntityConfig {
  resourceUrl = this.applicationConfigService.getEndpointFor('api/agents', 'bmms');

  constructor(private http: HttpClient, private entityService: EntityService, private applicationConfigService: ApplicationConfigService) {}

  create(agent: Agent): Observable<Agent> {
    return this.http.post<Agent>(this.resourceUrl, agent, { observe: 'body' });
  }

  createWithPassword(detailedAgent: DetailedAgent): Observable<Agent> {
    return this.http.post<Agent>(this.resourceUrl + '/save-with-user', detailedAgent, { observe: 'body' });
  }

  update(agent: Agent): Observable<Agent> {
    return this.http.put<Agent>(`${this.resourceUrl}/${agent.id}`, agent, { observe: 'body' });
  }

  find(id: number): Observable<Agent> {
    return this.http.get<Agent>(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  query(req?: any): Observable<HttpResponse<Agent[]>> {
    const options = createRequestOption(req);
    return this.http.get<Agent[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<{}> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'body' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'shortName.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsAgent.shortName',
      },
      { name: 'amountRatio.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsAgent.amountRatio'
      },
      {
        name: 'phoneNumber.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsAgent.phoneNumber',
        options: {
          mask: '+000 00 000 00 00',
        },
      },
      {
        name: 'inn.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsAgent.inn',
        options: {
          mask: '000000000',
        },
      },
      { name: 'orgName.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsAgent.orgName' },
      { name: 'email.contains', type: NglFilterFieldType.TEXT, translation: 'billMasterGwApp.pkbillmsAgent.email' },
    ];
  }

  openUpdate(agent?: Agent, duplicate?: boolean): void {
    const options: EntityUpdateOptions<AgentUpdateModalComponent> = {
      component: AgentUpdateModalComponent,
      componentInstances: {
        data: agent || {},
        duplicate: duplicate,
      },
    };

    this.entityService.update(options);
  }

  openView(agent: Agent): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: agent.id },
      { title: 'billMasterGwApp.pkbillmsAgent.shortName', value: agent.shortName },
      {
        title: 'billMasterGwApp.pkbillmsAgent.amountRatio',
        value: agent.amountRatio,
        type: 'translation',
        translation: 'global.amountRatio.' + agent.amountRatio,
      },
      { title: 'billMasterGwApp.pkbillmsAgent.apiKey', value: agent.apiKey },
      {
        title: 'billMasterGwApp.pkbillmsAgent.phoneNumber',
        value: agent.phoneNumber,
        type: 'mask',
        mask: '+000 00 000 00 00',
      },
      { title: 'billMasterGwApp.pkbillmsAgent.inn', value: agent.inn },
      { title: 'billMasterGwApp.pkbillmsAgent.orgName', value: agent.orgName },
      { title: 'billMasterGwApp.pkbillmsAgent.email', value: agent.email },
    ];

    this.entityService.view(options, agent.id, [Authority.AGENT_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.AGENT_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsAgent.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Agent',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsAgent',
    };

    this.entityService.history(options);
  }
}
