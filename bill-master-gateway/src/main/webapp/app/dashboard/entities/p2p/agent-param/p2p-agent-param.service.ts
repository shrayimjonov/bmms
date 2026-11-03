import { Injectable, isDevMode } from '@angular/core';
import { EntityService } from '../../../../shared/entity/entity.service';
import { IEntityConfig } from '../../../../shared/entity/entity-config.model';
import { EntityBaseService } from '../../../../shared/entity/entity-base.service';
import { P2pAgentParamModel } from './p2p-agent-param.model';
import { NglFilterField, NglFilterFieldType } from 'ngl-filter-field';
import { EntityUpdateOptions } from '../../../../shared/entity/entity-update/entity-update-options.model';
import { EntityViewOptions } from '../../../../shared/entity/entity-view/entity-view-options.model';
import { Authority } from '../../../../config/authority.constants';
import { Event } from '../../../../config/event.constants';
import { P2PAgentParamUpdateComponent } from './p2p-agent-param-update.component';
import { RegexTestComponent } from './regext-test/regext-test.component';

@Injectable({
  providedIn: 'root',
})
export class P2pAgentParamService extends EntityBaseService<P2pAgentParamModel> implements IEntityConfig {
  constructor(private entityService: EntityService) {
    super('api/agent-p-2-p-params', 'bmms');
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'agentId.contains',
        type: NglFilterFieldType.NUMBER,
        translation: 'billMasterGwApp.pkbillmsP2PCondition.value',
      },
      {
        name: 'active.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsSetting.active',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
      {
        name: 'description.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsP2PCondition.description',
      },
    ];
  }

  openUpdate(p2pCondition?: P2pAgentParamModel, duplicate?: boolean): void {
    const options: EntityUpdateOptions<P2PAgentParamUpdateComponent> = {
      component: P2PAgentParamUpdateComponent,
      componentInstances: {
        data: p2pCondition || {},
        duplicate: duplicate,
      },
    };

    this.entityService.update(options);
  }

  openView(p2pCondition: P2pAgentParamModel): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: p2pCondition.id },
      { title: 'billMasterGwApp.pkbillmsP2PAgentParam.agentId', value: p2pCondition.agentId },
      { title: 'billMasterGwApp.pkbillmsP2PAgentParam.p2pParamsRegex', value: p2pCondition.p2pParamsRegex },
      { title: 'billMasterGwApp.pkbillmsP2PAgentParam.active', value: p2pCondition.active, type: 'boolean', boolean: 'active' },
      { title: 'billMasterGwApp.pkbillmsP2PAgentParam.description', value: p2pCondition.description },
    ];

    this.entityService.view(options, p2pCondition.id, [Authority.P2P_AGENT_PARAM_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.P2P_AGENT_PARAM_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsP2PAgentParam.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  regexTest(regex: string): void {
    this.entityService.update({
      component: RegexTestComponent,
      componentInstances: {
        regex: regex,
      },
    });
  }
}
