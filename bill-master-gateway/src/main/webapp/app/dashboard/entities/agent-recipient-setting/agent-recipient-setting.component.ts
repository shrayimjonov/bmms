import {Component, OnDestroy, OnInit} from '@angular/core';
import {AgentRecipientSetting} from './agent-recipient-setting.model';
import {ActivatedRoute} from '@angular/router';
import {HttpResponse} from '@angular/common/http';
import {AgentRecipientSettingService} from './agent-recipient-setting.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'agent-recipient-setting.template.html'
})
export class AgentRecipientSettingComponent extends EntityBaseComponent<AgentRecipientSetting> implements OnInit, OnDestroy {
  constructor(
    public readonly settingService: AgentRecipientSettingService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.settingService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.SETTING_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.settingService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<AgentRecipientSetting[]>) => this.onDataReceived(response));
  }
}
