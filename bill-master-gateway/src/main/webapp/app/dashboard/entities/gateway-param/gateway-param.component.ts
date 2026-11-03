import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {GatewayParam} from './gateway-param.model';
import {GatewayParamService} from './gateway-param.service';
import {RecipientService} from '../recipient/recipient.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'gateway-param.template.html',
})
export class GatewayParamComponent extends EntityBaseComponent<GatewayParam> implements OnInit, OnDestroy {
  constructor(
    public readonly gatewayParamService: GatewayParamService,
    public readonly recipientService: RecipientService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.gatewayParamService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.GATEWAY_PARAM_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.gatewayParamService
      .query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<GatewayParam[]>) => this.onDataReceived(response));
  }
}
