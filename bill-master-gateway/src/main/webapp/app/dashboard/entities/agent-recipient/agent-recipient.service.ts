import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {createRequestOption} from '../../../core/util/request-util';
import {ApplicationConfigService} from '../../../core/config/application-config.service';
import {EntityViewOptions} from '../../../shared/entity/entity-view/entity-view-options.model';
import {Event} from '../../../config/event.constants';
import {EntityService} from '../../../shared/entity/entity.service';
import {AgentRecipientUpdateModalComponent} from './agent-recipient-update-modal.component';
import {AgentService} from '../agent/agent.service';
import {IEntityConfig} from '../../../shared/entity/entity-config.model';
import {RecipientService} from '../recipient/recipient.service';
import {RecipientGroupService} from '../recipient-group/recipient-group.service';
import {EntityUpdateOptions} from '../../../shared/entity/entity-update/entity-update-options.model';
import {NglFilterField, NglFilterFieldType} from 'ngl-filter-field';
import {AgentRecipient} from './agent-recipient.model';

type EntityResponseType = HttpResponse<AgentRecipient>;
type EntityArrayResponseType = HttpResponse<AgentRecipient[]>;

@Injectable({ providedIn: 'root' })
export class AgentRecipientService implements IEntityConfig {
  resourceUrl = this.applicationConfigService.getEndpointFor('api/agent-recipients', 'bmms');

  constructor(
    private http: HttpClient,
    private entityService: EntityService,
    private agentService: AgentService,
    private recipientService: RecipientService,
    private recipientGroupService: RecipientGroupService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(agentRecipient: AgentRecipient): Observable<EntityResponseType> {
    const url = this.resourceUrl + (agentRecipient.recipientGroup ? '/group' : '');
    return this.http.post<AgentRecipient>(url, agentRecipient, { observe: 'response' });
  }

  update(agentRecipient: AgentRecipient): Observable<EntityResponseType> {
    return this.http.put<AgentRecipient>(`${this.resourceUrl}/${agentRecipient.id}`, agentRecipient, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<AgentRecipient>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<AgentRecipient[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'agentId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsAgentRecipient.agent',
        options: {
          'select.resourceUrl': this.agentService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'recipientId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsAgentRecipient.recipient',
        options: {
          'select.resourceUrl': this.recipientService.resourceUrl,
          'select.nzLabel': 'shortName',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'agentCommission.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsAgentRecipient.agentCommission',
      },
      {
        name: 'agentFee.equals',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsAgentRecipient.agentFee',
      },
      {
        name: 'requestGroup.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsAgentRecipient.requestGroup',
      },
      {
        name: 'recipientGroupId.in',
        type: NglFilterFieldType.MULTI_SELECT,
        translation: 'billMasterGwApp.pkbillmsAgentRecipient.recipientGroup',
        options: {
          'select.resourceUrl': this.recipientGroupService.resourceUrl,
          'select.nzLabel': 'name',
          'select.nzValue': 'id',
        },
      },
      {
        name: 'jsonParams.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsAgentRecipient.jsonParams',
      },
    ];
  }

  openUpdate(agentRecipient?: AgentRecipient, duplicate?: boolean): void {
    const options: EntityUpdateOptions<AgentRecipientUpdateModalComponent> = {
      component: AgentRecipientUpdateModalComponent,
      componentInstances: {
        data: agentRecipient || {},
        duplicate: duplicate
      },
      modalOptions: { size: 'lg' },
    };

    this.entityService.update(options);
  }

  openView(agentRecipient: AgentRecipient): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: agentRecipient.id },
      {
        title: 'billMasterGwApp.pkbillmsAgentRecipient.agent',
        value: agentRecipient.agent?.shortName,
        type: 'link',
        link: () => this.agentService.openView(agentRecipient.agent),
      },
      {
        title: 'billMasterGwApp.pkbillmsAgentRecipient.recipient',
        value: agentRecipient.recipient?.shortName,
        type: 'link',
        link: () => this.recipientService.openView(agentRecipient.recipient),
      },
      { title: 'billMasterGwApp.pkbillmsAgentRecipient.agentCommission', value: agentRecipient.agentCommission },
      { title: 'billMasterGwApp.pkbillmsAgentRecipient.agentFee', value: agentRecipient.agentFee },
      { title: 'billMasterGwApp.pkbillmsAgentRecipient.systemCommissionUp', value: agentRecipient.systemCommissionUp },
      {
        title: 'billMasterGwApp.pkbillmsAgentRecipient.systemCommissionDown',
        value: agentRecipient.systemCommissionDown,
      },
      { title: 'billMasterGwApp.pkbillmsAgentRecipient.systemFee', value: agentRecipient.systemFee },
      {
        title: 'billMasterGwApp.pkbillmsAgentRecipient.active',
        value: agentRecipient.active,
        type: 'boolean',
        boolean: 'active',
      },
      { title: 'billMasterGwApp.pkbillmsAgentRecipient.requestGroup', value: agentRecipient.requestGroup },
      {
        title: 'billMasterGwApp.pkbillmsAgentRecipient.recipientGroup',
        value: agentRecipient.recipientGroup?.name,
        type: 'link',
        link: () => this.recipientGroupService.openView(agentRecipient.recipientGroup),
      },
      {
        title: 'billMasterGwApp.pkbillmsAgentRecipient.jsonParams',
        value: JSON.parse(agentRecipient.jsonParams),
        type: 'json',
      },
    ];

    this.entityService.view(options, agentRecipient.id);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.AGENT_RECIPIENT_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsAgentRecipient.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'AgentRecipient',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsAgentRecipient',
    };

    this.entityService.history(options);
  }
}
