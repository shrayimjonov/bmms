import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {P2pParam} from './p2p-param.model';
import {P2pParamService} from './p2p-param.service';
import {P2pConditionService} from '../condition/p2p-condition.service';
import {P2pCommissionService} from '../commission/p2p-commission.service';
import {Event} from '../../../../config/event.constants';
import {EntityUpdateOptions} from '../../../../shared/entity/entity-update/entity-update-options.model';
import {EntityService} from '../../../../shared/entity/entity.service';
import {P2pParamTestModalComponent} from './test/p2p-param-test-modal.component';
import {EntityBaseComponent} from '../../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'p2p-param.template.html',
})
export class P2pParamComponent extends EntityBaseComponent<P2pParam> implements OnInit, OnDestroy {
  constructor(
    public readonly p2pParamService: P2pParamService,
    public readonly p2pConditionService: P2pConditionService,
    public readonly p2pCommissionService: P2pCommissionService,
    private readonly entityService: EntityService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.p2pParamService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.P2P_PARAM_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.p2pParamService.query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<P2pParam[]>) => this.onDataReceived(response));
  }

  extractPropertyFromJSON(jsonData: string, property: string): string | null {
    if (jsonData) {
      return JSON.parse(jsonData)![property];
    }
    return null;
  }

  openTestModal(): void {
    const options: EntityUpdateOptions<P2pParamTestModalComponent> = {
      component: P2pParamTestModalComponent,
    };

    this.entityService.update(options);
  }
}
