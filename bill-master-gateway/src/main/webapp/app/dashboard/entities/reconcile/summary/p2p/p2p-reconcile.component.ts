import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import * as moment from 'moment';
import {EntityBaseComponent} from '../../../../../shared/entity/entity-base-component';
import {NglFilterFieldType} from 'ngl-filter-field';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Event} from "../../../../../config/event.constants";
import {P2PGatewayType, ReconcileSummary, ReconcileType} from "../reconcile-summary.model";
import {ReconcileSummaryService} from "../reconcile-summary.service";
import {P2pCardReconcileModalComponent} from "./card/p2p-card-reconcile-modal.component";

@Component({
  templateUrl: 'p2p-reconcile.template.html',
  styleUrls: ['../../reconcile-status.style.scss'],
})
export class P2pReconcileComponent extends EntityBaseComponent<ReconcileSummary> implements OnInit, OnDestroy {
  constructor(public readonly reconcileSummaryService: ReconcileSummaryService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly modalService: NgbModal) {
    super();
  }

  ngOnInit(): void {
    this.handleNavigation(this.activatedRoute);

    this.filterFields = this.reconcileSummaryService.getFilterFields();
    this.filterFields.forEach(field => {
      if (field.name === 'gatewayName.in') {
        field.type = NglFilterFieldType.MULTI_SELECT;
        field.translation = 'billMasterGwApp.pkbillmsReconcileSummary.p2pType';
        field.options = {
          'select.items': Object.keys(P2PGatewayType).map(key => P2PGatewayType[key]),
          "select.nzOptionOverflowSize": 12
        }
      }
    });

    this.registerToModify(Event.RECONCILE_SUMMARY_LIST_MODIFICATION, () => this.loadPage());
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  loadPage(page?: number): void {
    const req = {
      ...this.getRequestParams(page),
      ['type.equals']: ReconcileType.P2P
    };

    this.loading = true;
    this.reconcileSummaryService.query(req)
      .pipe(
        this.loadPageOperators()
      )
      .subscribe((response: HttpResponse<ReconcileSummary[]>) => this.onDataReceived(response),);
  }

  filter(query: any): void {
    const from = query['date.greaterThanOrEqual'];
    const to = query['date.lessThanOrEqual'];

    if (from) {
      query['date.greaterThanOrEqual'] = moment(from).format('YYYY-MM-DD')
    }

    if (to) {
      query['date.lessThanOrEqual'] = moment(to).format('YYYY-MM-DD');
    }

    this.query = query;
    this.loadPage();
  }

  openReconcileModal(): void {
    this.modalService.open(P2pCardReconcileModalComponent, {size: 'sm', backdrop: 'static'});
  }
}
