import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { createRequestOption } from '../../../core/util/request-util';
import { AgentRecipientSetting } from './agent-recipient-setting.model';
import { ApplicationConfigService } from '../../../core/config/application-config.service';
import { IEntityConfig } from '../../../shared/entity/entity-config.model';
import { EntityService } from '../../../shared/entity/entity.service';
import { EntityUpdateOptions } from '../../../shared/entity/entity-update/entity-update-options.model';
import { EntityViewOptions } from '../../../shared/entity/entity-view/entity-view-options.model';
import { Event } from '../../../config/event.constants';
import { AgentRecipientSettingUpdateModalComponent } from './agent-recipient-setting-update-modal.component';
import { PcSetting } from './pc-setting.model';
import { AgentRecipientSettingResolver } from './agent-recipient-setting-resolver.model';
import { NglFilterField, NglFilterFieldType } from 'ngl-filter-field';
import { Authority } from '../../../config/authority.constants';

type EntityResponseType = HttpResponse<AgentRecipientSetting>;
type EntityArrayResponseType = HttpResponse<AgentRecipientSetting[]>;

@Injectable({ providedIn: 'root' })
export class AgentRecipientSettingService implements IEntityConfig {
  public resourceUrl = this.applicationConfigService.getEndpointFor('api/settings', 'bmms');
  public resourceParentUrl = this.applicationConfigService.getEndpointFor('/api/setting-parents', 'bmms');

  constructor(
    protected http: HttpClient,
    private entityService: EntityService,
    private applicationConfigService: ApplicationConfigService
  ) {}

  create(setting: AgentRecipientSetting): Observable<EntityResponseType> {
    return this.http.post<AgentRecipientSetting>(this.resourceUrl, setting, { observe: 'response' });
  }

  update(setting: AgentRecipientSetting): Observable<EntityResponseType> {
    return this.http.put<AgentRecipientSetting>(`${this.resourceUrl}/${setting.id}`, setting, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<AgentRecipientSetting>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<AgentRecipientSetting[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  resolve(resolver: AgentRecipientSettingResolver): Observable<PcSetting> {
    return this.http.post<PcSetting>(this.applicationConfigService.getEndpointFor('api/setting-resolve', 'bmms'), resolver, {
      observe: 'body',
    });
  }

  getFilterFields(): NglFilterField[] {
    return [
      { name: 'id.equals', type: NglFilterFieldType.NUMBER, translation: 'global.field.id' },
      {
        name: 'shortName.contains',
        type: NglFilterFieldType.TEXT,
        translation: 'billMasterGwApp.pkbillmsSetting.shortName',
      },
      { name: 'ord.equals', type: NglFilterFieldType.NUMBER, translation: 'billMasterGwApp.pkbillmsSetting.ord' },
      {
        name: 'active.equals',
        type: NglFilterFieldType.SELECT,
        translation: 'billMasterGwApp.pkbillmsSetting.active',
        options: {
          'select.nzLabelTranslation': 'global.active',
          'select.items': [true, false],
        },
      },
    ];
  }

  openUpdate(setting?: AgentRecipientSetting, duplicate?: boolean): void {
    const options: EntityUpdateOptions<AgentRecipientSettingUpdateModalComponent> = {
      component: AgentRecipientSettingUpdateModalComponent,
      componentInstances: {
        data: setting || {},
        duplicate: duplicate,
      },
      modalOptions: { size: 'lg' },
    };

    this.entityService.update(options);
  }

  openView(setting: AgentRecipientSetting): void {
    const options: EntityViewOptions[] = [
      { title: 'global.field.id', value: setting.id },
      { title: 'billMasterGwApp.pkbillmsSetting.shortName', value: setting.shortName },
      { title: 'billMasterGwApp.pkbillmsSetting.ord', value: setting.ord },
      { title: 'billMasterGwApp.pkbillmsSetting.active', value: setting.active, type: 'boolean', boolean: 'active' },
    ];

    this.entityService.view(options, setting.id, [Authority.RECIPIENT_READ]);
  }

  openDelete(id: number): void {
    const options = {
      useFunction: this.delete(id),
      event: Event.SETTING_LIST_MODIFICATION,
      alertTranslation: 'pkbillmsApp.pkbillmsSetting.deleted',
      alertTranslationValue: id,
    };

    this.entityService.delete(options);
  }

  openHistory(id: number): void {
    const options = {
      qualifiedName: 'Setting',
      entityId: id,
      i18nPrefix: 'billMasterGwApp.pkbillmsSetting',
    };

    this.entityService.history(options);
  }
}
