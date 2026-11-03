import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {Gateway} from './gateway.model';
import {GatewayService} from './gateway.service';
import {Event} from '../../../config/event.constants';
import {EntityBaseComponent} from '../../../shared/entity/entity-base-component';

@Component({
  templateUrl: 'gateway.template.html',
})
export class GatewayComponent extends EntityBaseComponent<Gateway> implements OnInit, OnDestroy {
  constructor(
    public readonly gatewayService: GatewayService,
    private readonly activatedRoute: ActivatedRoute,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filterFields = this.gatewayService.getFilterFields();
    this.handleNavigation(this.activatedRoute);
    this.registerToModify(Event.GATEWAY_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    this.loading = true;
    this.gatewayService
      .query(this.getRequestParams(page))
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<Gateway[]>) => this.onDataReceived(response));
  }
}
